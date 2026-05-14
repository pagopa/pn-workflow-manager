package it.pagopa.pn.workflowmanager.config.springbootcfg;


import it.pagopa.pn.commons.log.MDCRequestFilter;
import org.springframework.stereotype.Component;

@Component
public class MDCTraceIdWebFilter extends MDCRequestFilter {

}