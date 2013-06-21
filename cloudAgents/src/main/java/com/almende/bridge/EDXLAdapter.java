package com.almende.bridge;

import com.almende.eve.agent.AgentInterface;
import com.almende.eve.rpc.annotation.Name;

public interface EDXLAdapter extends AgentInterface {
	/**
	 * (inbound)<br>
	 * 
	 * 3.4 RequestResource Message The “RequestResource” message is used as an
	 * announcement to a broad audience of potential suppliers as well as
	 * potential suppliers in the local geographic area of interest. It is
	 * intended to be used by Emergency Managers, Incident Commanders and other
	 * First Responders to request information on availability of needed
	 * resources.<br>
	 * <br>
	 * (outbound)<br>
	 * 
	 * 3.5 ResponseToRequestResource Message The “ResponseToRequestResource”
	 * message is used by potential resource suppliers (e.g. mutual aid
	 * partners, equipment suppliers, etc.) to respond to RequestResource
	 * messages from Emergency Managers, Incident Commanders and First
	 * Responders or others with logistics responsibilities. The response may
	 * identify availability, limitations and other pertinent information
	 * related to resources in the original request.
	 * 
	 * 
	 * @param message
	 *            RequestResourceMessage (EDXL-RM 3.4)
	 * @return ResponseToRequestResourceMessage (EDXL-RM 3.5)
	 * @throws Exception 
	 */
	public String RequestResource(@Name("RequestResourceMessage") String message) throws Exception;

	/**
	 * (inbound)<br>
	 * 
	 * 3.6 RequisitionResource Message The “RequisitionResource” message is used
	 * by Resource Consumers to order resources from Resource Suppliers. These
	 * may relate to one or more responses to a previous Request Resource
	 * message<br>
	 * <br>
	 * (outbound)<br>
	 * 
	 * 3.7 CommitResource Message The “CommitResource” message is used by a
	 * Resource Supplier to confirm that resources have been committed to a
	 * Resource Consumer request. Usually, the CommitResource is in response to
	 * a RequisitionResource, or even a RequestResource. The CommitResource is
	 * the only message used to indicate the resources have been allocated to an
	 * assignment/incident.
	 * 
	 * @param message
	 *            RequisitionResourceMessage (EDXL-RM 3.6)
	 * @return CommitResourceMessage (EDXL-RM 3.7)
	 * @throws Exception 
	 */
	public String RequisitionResource(
			@Name("RequisitionResourceMessage") String message) throws Exception;

	/*
	 * (outbound) Optional
	 * 
	 * 3.10 OfferUnsolicitedResource Message The “OfferUnsolicitedResource”
	 * message is used to offer available resources (that have not been
	 * requested) to assist with an emergency response
	 */

	/**
	 * (inbound)<br>
	 * 
	 * 3.11 ReleaseResource Message The “ReleaseResource” message is used by
	 * authorities at the incident to “release” (demobilize) a resource back to
	 * its original point of assignment or to another location / assignment.
	 * 
	 * @param message ReleaseResourceMessage (EDXL-RM 3.11)
	 * @return "OK"
	 * @throws Exception
	 */
	public String ReleaseResource(@Name("ReleaseResourceMessage") String message) throws Exception;
	
	/*
	 * (outbound)
	 * 
	 * 9 3.12 RequestReturn Message The “RequestReturn” message is used to
	 * request release (demobilization) of resource(s) back to its original
	 * owning jurisdiction and location or to another location / assignment.
	 * 
	 * (inbound)
	 * 
	 * 3.13 ResponseToRequestReturn Message The “ResponseToRequestReturn”
	 * message is used by Resource Consumers to respond to a RequestReturn
	 * message from Resource Suppliers. The response identifies the resources in
	 * the original request message and how the Resource Consumer has responded
	 */

	/**
	 * (inbound)<br>
	 * 
	 * 3.16 RequestResourceDeploymentStatus Message The
	 * “RequestResourceDeploymentStatus” message is used to request the current
	 * status of one or more deployed resources. It can be sent by the Resource
	 * Supplier to the Resource Consumer (e.g., to check the status of the
	 * resource after a “ReleaseResource” message) or by the Resource Consumer
	 * to the Resource Supplier (e.g., to track the progress of a resource after
	 * a “RequisitionResource” message).<br>
	 * <br>
	 * (outbound)<br>
	 * 
	 * 3.17 ReportResourceDeploymentStatus Message The
	 * “ReportResourceDeploymentStatus” message is used to report on the current
	 * status of any deployed resource. The message can be sent from the
	 * Resource Supplier to the Resource Consumer, or from the Resource Consumer
	 * to the Resource Supplier.
	 * 
	 * @param message
	 *            RequestResourceDeploymentStatusMessage (EDXL-RM 3.16)
	 * @return ReportResourceDeploymentStatusMessage (EDXL-RM 3.17)
	 * @throws Exception 
	 */
	public String RequestResourceDeploymentStatus(
			@Name("RequestResourceDeploymentStatusMessage") String message) throws Exception;

}
