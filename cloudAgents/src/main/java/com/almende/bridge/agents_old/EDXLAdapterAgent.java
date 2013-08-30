package com.almende.bridge.agents_old;

import static com.almende.bridge.edxl.EDXLGenerator.setElementWithPath;
import static com.almende.bridge.edxl.EDXLParser.getElementsByType;
import static com.almende.bridge.edxl.EDXLParser.getStringByPath;
import static com.almende.bridge.edxl.EDXLParser.parseXML;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.almende.bridge.EDXLAdapter;
import com.almende.bridge.edxl.EDXLGenerator;
import com.almende.bridge.edxl.EDXLParser;
import com.almende.bridge.edxl.EDXLParser.EDXLRet;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Required;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class EDXLAdapterAgent extends Agent implements EDXLAdapter {
	static String		lastTaskId	= "";
	static final int	RESPERMES	= 1;
	
	public void notify(@Name("data") String data,
			@Required(false) @Name("producerId") String producer,
			@Required(false) @Name("itemId") String itemId,
			@Name("meta") JsonNode meta) throws Exception {
		System.err.println("Received notify:" + data + " : " + producer + " : "
				+ itemId + " : " + meta.toString());
		RequestResource(data);
	}
	
	public ArrayNode getResources() {
		ArrayNode result = JOM.createArrayNode();
		try {
			ArrayNode allResources = send(URI.create("local://demo"),
					"getAllResources", ArrayNode.class);
			
			Iterator<JsonNode> iter = allResources.elements();
			while (iter.hasNext()) {
				ArrayNode subList = JOM.createArrayNode();
				subList.add(iter.next());
				String replyDoc = createReportResourceDeploymentStatus(subList);
				result.add(replyDoc);
			}
		} catch (Exception e) {
			System.err
					.println("Ran into trouble creating and posting EDXL-RM for S2D2S.");
			e.printStackTrace();
		}
		return result;
	}
	
	public void sendReportResourceDeploymentStatus(
			@Required(false) @Name("interval") Integer interval) {
		try {
			ArrayNode allResources = send(URI.create("local://demo"),
					"getAllResources", ArrayNode.class);
			
			Iterator<JsonNode> iter = allResources.elements();
			ArrayNode subList = JOM.createArrayNode();
			int count = 1;
			while (iter.hasNext()) {
				subList.add(iter.next());
				if (subList.size() >= RESPERMES) {
					String replyDoc = createReportResourceDeploymentStatus(subList);
					
					ObjectNode params = JOM.createObjectNode();
					params.put("topic", "App.Global.ResourceStatus");
					params.put("contentType", "application/xml");
					params.put("metadata", "");
					params.put("itemId", count++);
					params.put("persist", "true");
					params.put("payLoad", replyDoc);
					
					send(URI.create("http://bridge.d-cis.nl:8008/Name/S2D2S/jsonrpc"),
							"publish", params);
					
					subList = JOM.createArrayNode();
				}
			}
		} catch (Exception e) {
			System.err
					.println("Ran into trouble creating and posting EDXL-RM for S2D2S.");
			e.printStackTrace();
		}
		if (interval != null) {
			ObjectNode params = JOM.createObjectNode();
			params.put("interval", interval.toString());
			lastTaskId = getScheduler().createTask(
					new JSONRequest("sendReportResourceDeploymentStatus",
							params), interval * 1000);
		}
	}
	
	public void stop() {
		getScheduler().cancelTask(lastTaskId);
	}
	
	@Override
	public String RequestResource(@Name("RequestResourceMessage") String message)
			throws Exception {
		ObjectNode task = JOM.createObjectNode();
		ArrayNode resources = JOM.createArrayNode();
		
		EDXLRet inDoc = parseXML(message);
		if (inDoc == null) throw new Exception("Failed to parse XML message.");
		if (!"RequestResource".equalsIgnoreCase(inDoc.getMsgType())) throw new Exception(
				"Incorrect XML message type!");
		
		String messageID = getStringByPath(inDoc.getRoot(), "MessageID");
		task.put("messageID", messageID);
		task.put(
				"IncidentDescription",
				getStringByPath(inDoc.getRoot(), new String[] {
						"IncidentInformation", "IncidentDescription" }));
		
		List<Element> resList = getElementsByType(inDoc.getRoot(),
				"ResourceInformation");
		for (Element res : resList) {
			ObjectNode node = JOM.createObjectNode();
			node.put(
					"resourceType",
					getStringByPath(res, new String[] { "Resource",
							"TypeStructure", "Value" }));
			node.put(
					"resourceID",
					getStringByPath(res, new String[] { "Resource",
							"ResourceID" }));
			node.put(
					"amountString",
					getStringByPath(res, new String[] {
							"AssignmentInformation", "Quantity",
							"MeasuredQuantity", "Amount" }));
			node.put(
					"anticipatedFunction",
					getStringByPath(res, new String[] {
							"AssignmentInformation", "AnticipatedFunction" }));
			node.put(
					"assignmentInstructions",
					getStringByPath(res, new String[] {
							"AssignmentInformation", "AssignmentInstructions" }));
			node.put(
					"scheduleType",
					getStringByPath(res, new String[] { "ScheduleInformation",
							"ScheduleType" }));
			node.put(
					"scheduleDate",
					getStringByPath(res, new String[] { "ScheduleInformation",
							"DateTime" }));
			// TODO: handle other notations for GeoData, depending on namespace
			// definition? Currently only GML is supported.
			node.put(
					"scheduleLocation",
					getStringByPath(res, new String[] { "ScheduleInformation",
							"location", "TargetArea", "Point", "pos" }));
			
			resources.add(node);
		}
		task.put("resources", resources);
		
		// Create Task agent:
		AgentHost factory = AgentHost.getInstance();
		if (factory.hasAgent("Task_" + messageID)) {
			throw new Exception(
					"Task has already been posted before, please use a new messageID!");
		}
		SimpleTaskAgent agent = (SimpleTaskAgent) factory
				.createAgent("com.almende.bridge.agent.SimpleTaskAgent",
						"Task_" + messageID);
		agent.prepare(task);
		
		// Prepare response message:
		Document replyDoc = EDXLGenerator.genDoc("ResponseToRequestResource");
		Element root = replyDoc.getRootElement();
		setElementWithPath(root, new String[] { "OriginatingMessageID" },
				messageID);
		setElementWithPath(root, new String[] { "PrecedingMessageID" },
				messageID);
		// Report agent ID in messageDescription:
		setElementWithPath(root, new String[] { "MessageDescription" },
				agent.getId());
		int count = 1;
		for (Element res : resList) {
			Element sub = new Element("ResourceInformation");
			setElementWithPath(sub, new String[] { "ResourceInfoElementID" },
					new Integer(count++).toString());
			setElementWithPath(
					sub,
					new String[] { "ReponseInformation",
							"PrecedingResourceInfoElementID" },
					getStringByPath(res,
							new String[] { "ResourceInfoElementID" }));
			setElementWithPath(sub, new String[] { "ReponseInformation",
					"ResponseType" }, "Provisional");
			String resourceID = getStringByPath(res, new String[] { "Resource",
					"ResourceID" });
			if (!resourceID.equals("")) {
				setElementWithPath(sub,
						new String[] { "Resource", "ResourceID" }, resourceID);
			}
			
			root.addContent(sub);
		}
		return EDXLGenerator.printDoc(replyDoc);
	}
	
	@Override
	public String RequisitionResource(
			@Name("RequisitionResourceMessage") String message)
			throws Exception {
		EDXLRet inDoc = EDXLParser.parseXML(message);
		if (inDoc == null) throw new Exception("Failed to parse XML message.");
		if (!"RequisitionResource".equalsIgnoreCase(inDoc.getMsgType())) throw new Exception(
				"Incorrect XML message type!");
		ObjectNode task = JOM.createObjectNode();
		ArrayNode resources = JOM.createArrayNode();
		String messageID = getStringByPath(inDoc.getRoot(), "MessageID");
		task.put("messageID", messageID);
		task.put(
				"IncidentDescription",
				getStringByPath(inDoc.getRoot(), new String[] {
						"IncidentInformation", "IncidentDescription" }));
		
		List<Element> resList = getElementsByType(inDoc.getRoot(),
				"ResourceInformation");
		for (Element res : resList) {
			ObjectNode node = JOM.createObjectNode();
			node.put(
					"resourceType",
					getStringByPath(res, new String[] { "Resource",
							"TypeStructure", "Value" }));
			node.put(
					"resourceID",
					getStringByPath(res, new String[] { "Resource",
							"ResourceID" }));
			node.put(
					"amountString",
					getStringByPath(res, new String[] {
							"AssignmentInformation", "Quantity",
							"MeasuredQuantity", "Amount" }));
			node.put(
					"anticipatedFunction",
					getStringByPath(res, new String[] {
							"AssignmentInformation", "AnticipatedFunction" }));
			node.put(
					"assignmentInstructions",
					getStringByPath(res, new String[] {
							"AssignmentInformation", "AssignmentInstructions" }));
			node.put(
					"scheduleType",
					getStringByPath(res, new String[] { "ScheduleInformation",
							"ScheduleType" }));
			node.put(
					"scheduleDate",
					getStringByPath(res, new String[] { "ScheduleInformation",
							"DateTime" }));
			// TODO: handle other notations for GeoData, depending on namespace
			// definition? Currently only GML is supported.
			node.put(
					"scheduleLocation",
					getStringByPath(res, new String[] { "ScheduleInformation",
							"location", "TargetArea", "Point", "pos" }));
			
			resources.add(node);
		}
		task.put("resources", resources);
		
		// Create Task agent:
		AgentHost factory = AgentHost.getInstance();
		if (factory.hasAgent("Task_" + messageID)) {
			throw new Exception(
					"Task has already been posted before, please use a new messageID!");
		}
		SimpleTaskAgent agent = (SimpleTaskAgent) factory
				.createAgent("com.almende.bridge.agent.SimpleTaskAgent",
						"Task_" + messageID);
		agent.prepare(task);
		
		Document replyDoc = EDXLGenerator.genDoc("CommitResource");
		return EDXLGenerator.printDoc(replyDoc);
	}
	
	@Override
	public String ReleaseResource(@Name("ReleaseResourceMessage") String message)
			throws Exception {
		EDXLRet inDoc = EDXLParser.parseXML(message);
		if (inDoc == null) throw new Exception("Failed to parse XML message.");
		if (!"ReleaseResource".equalsIgnoreCase(inDoc.getMsgType())) throw new Exception(
				"Incorrect XML message type!");
		
		List<Element> resList = getElementsByType(inDoc.getRoot(),
				"ResourceInformation");
		for (Element res : resList) {
			String resourceID = getStringByPath(res, new String[] { "Resource",
					"ResourceID" });
			if (!resourceID.equals("")) {
				send(URI.create("local://" + resourceID), "release");
			} else {
				System.err
						.println("Warning: resourceID not found in ReleaseResourceMessage.");
			}
		}
		return "OK";
	}
	

	public String createReportResourceDeploymentStatus(
			@Name("members") ArrayNode members) {
		Document replyDoc = EDXLGenerator
				.genDoc("ReportResourceDeploymentStatus");
		Element root = replyDoc.getRootElement();
		boolean sendTasks = true;
		
		if (members != null) {
			int count = 1;
			for (JsonNode res : members) {
				ObjectNode member = (ObjectNode) res;
				String resourceUrl = member.get("url").textValue();
				try {
					ObjectNode status = send(URI.create(resourceUrl), "requestStatus",null,ObjectNode.class);
					if (status == null) {
						throw new Exception("Status null!" + resourceUrl);
					} else {
						System.err.println("Status:" + status);
					}
					Element sub = new Element("ResourceInformation");
					setElementWithPath(sub,
							new String[] { "ResourceInfoElementID" },
							new Integer(count++).toString());
					setElementWithPath(sub, new String[] { "Resource",
							"ResourceID" }, status.get("id").textValue());
					setElementWithPath(sub,
							new String[] { "Resource", "Name" },
							status.get("name").textValue());
					setElementWithPath(sub, new String[] { "Resource",
							"TypeStructure", "rm:Value" }, status.get("type")
							.textValue());
					setElementWithPath(sub, new String[] { "Resource",
							"TypeStructure", "rm:ValueListURN" },
							"urn:x-hazard:vocab:resourceTypes");
					if (status.has("deploymentStatus")) {
						setElementWithPath(sub, new String[] { "Resource",
								"ResourceStatus", "DeploymentStatus",
								"rm:Value" }, status.get("deploymentStatus")
								.asText());
						setElementWithPath(sub, new String[] { "Resource",
								"ResourceStatus", "DeploymentStatus",
								"rm:ValueListURN" },
								"urn:x-hazard:vocab:deploymentStatusTypes");
					}
					if (status.has("current")) {
						Element schedule = new Element("ScheduleInformation");
						ObjectNode loc = (ObjectNode) status.get("current");
						setElementWithPath(schedule,
								new String[] { "ScheduleType" }, "Current");
						setElementWithPath(schedule, new String[] { "Location",
								"rm:TargetArea", "gml:Point", "gml:pos" },
								loc.get("latitude").textValue() + " "
										+ loc.get("longitude").textValue());
						if (loc.has("time")) {
							String time = loc.get("time").textValue();
							if (time != null && !time.isEmpty()) {
								setElementWithPath(schedule,
										new String[] { "DateTime" },
										loc.get("time").textValue());
							}
						}
						sub.addContent(schedule);
					}
					if (sendTasks && status.has("goal")) {
						Element schedule = new Element("ScheduleInformation");
						ObjectNode loc = (ObjectNode) status.get("goal");
						setElementWithPath(schedule,
								new String[] { "ScheduleType" },
								"RequestedArrival");
						setElementWithPath(schedule, new String[] { "Location",
								"rm:TargetArea", "gml:Point", "gml:pos" },
								loc.get("latitude").textValue() + " "
										+ loc.get("longitude").textValue());
						if (loc.has("time")) {
							String time = loc.get("time").textValue();
							
							if (time != null && !time.isEmpty()) {
								setElementWithPath(schedule,
										new String[] { "DateTime" },
										loc.get("time").textValue());
							}
						}
						sub.addContent(schedule);
					}
					if (sendTasks && status.has("task")) {
						setElementWithPath(sub,
								new String[] { "AssignmentInformation",
										"AnticipatedFunction" },
								status.get("task").textValue());
					}
					root.addContent(sub);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return EDXLGenerator.printDoc(replyDoc);
	}
	
	@Override
	public String getDescription() {
		return "EDXL-RM adapter for communication with MasterTable through S2D2S.";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
}
