package au.org.ala.cas.webflow

import au.org.ala.utils.logger
import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer
import org.apereo.cas.web.flow.CasWebflowConstants
import org.apereo.cas.web.flow.DefaultWebflowConfigurer
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry
import org.springframework.webflow.engine.ActionState
import org.springframework.webflow.engine.Flow
import org.springframework.webflow.engine.builder.support.FlowBuilderServices

class AlaCasWebflowConfigurer(
        flowBuilderServices: FlowBuilderServices,
        loginFlowDefinitionRegistry: FlowDefinitionRegistry,
        logoutFlowDefinitionRegistry: FlowDefinitionRegistry,
        val generateAuthCookieAction: GenerateAuthCookieAction,
        val removeAuthCookieAction: RemoveAuthCookieAction,
        val initializeLoginAction: AlaInitializeLoginAction) :
        AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry) {

    init {
        this.logoutFlowDefinitionRegistry = logoutFlowDefinitionRegistry
    }

    companion object {
        val log = logger<AlaCasWebflowConfigurer>()
    }

    override fun doInitialize() {
        log.info("doInitialize()")

        val inFlow = loginFlow
        val outFlow = logoutFlow

        if (inFlow != null) {
            doGenerateAuthCookieOnLoginAction(inFlow)
            doInitWebflow(inFlow)
        }

        if (outFlow != null) {
            doRemoveAuthCookieOnLogoutAction(outFlow)
            doInitWebflow(outFlow)
        }
    }

    private fun doInitWebflow(flow: Flow) {
        flow.startActionList.add(initializeLoginAction)
    }

    private fun doGenerateAuthCookieOnLoginAction(flow: Flow) {
        val sendTicketGrantingTicketAction = flow.getState(CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET) as ActionState
        sendTicketGrantingTicketAction.exitActionList.add(generateAuthCookieAction)
    }

    private fun doRemoveAuthCookieOnLogoutAction(flow: Flow) {
        val doLogoutAction = flow.getState(CasWebflowConstants.STATE_ID_DO_LOGOUT) as ActionState
        doLogoutAction.entryActionList.add(removeAuthCookieAction)
    }

}