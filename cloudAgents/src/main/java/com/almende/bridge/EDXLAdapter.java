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
	

}
