package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class PnSendModeUtils {
    public static final String SEPARATOR = ";";
    public static final int INDEX_START_DATE = 0;
    public static final int PEC_SEND_ATTACHMENT_MODE_INDEX = 2;
    public static final int EMAIL_SEND_ATTACHMENT_MODE_INDEX = 3;
    public static final int SIMPLE_REGISTERED_LETTER_SEND_ATTACHMENT_MODE_INDEX = 4;
    private final List<PnSendMode> pnSendModesList;
    
    public PnSendModeUtils(PnWorkflowManagerConfigs pnWorkflowManagerConfigs){
        List<PnSendMode> pnSendModesListNotSorted = getPnSendModeFromString(pnWorkflowManagerConfigs.getPnSendMode());
        pnSendModesList = getSortedList(pnSendModesListNotSorted);
    }
    
    public PnSendMode getPnSendMode(Instant time){
        log.debug("Start getPnSendMode for time={}", time);
        PnSendMode pnSendMode =  getCorrectPnSendModeFromDate(time, pnSendModesList);
        log.debug("End getPnSendMode. PnSendMode for time={} is {}", time, pnSendMode);
        return pnSendMode;
    }
    
    private PnSendMode getCorrectPnSendModeFromDate(Instant time, List<PnSendMode> pnSendModesList) {
        for(int i = pnSendModesList.size() - 1; i >=0; i--){
            PnSendMode elem = pnSendModesList.get(i);
            if( time.isAfter(elem.getStartConfigurationTime()) || time.equals(elem.getStartConfigurationTime()) ){
                return elem;
            }
        }
        return null;
    }

    @NotNull
    private static List<PnSendMode> getSortedList(List<PnSendMode> pnSendModesList) {
        List<PnSendMode> pnSendModesListSorted = new ArrayList<>(pnSendModesList);
        Collections.sort(pnSendModesListSorted);
        log.debug("PnSendModesListSorted is {}", pnSendModesListSorted);
        return pnSendModesListSorted;
    }

    private List<PnSendMode> getPnSendModeFromString(List<String> pnSendModeStringList) {
        return pnSendModeStringList.stream().map(elem -> {
            String[] parts = elem.split(SEPARATOR);
            return PnSendMode.builder()
                    .startConfigurationTime(Instant.parse(parts[INDEX_START_DATE]))
                    .pecSendAttachmentMode(SendAttachmentMode.fromValue(parts[PEC_SEND_ATTACHMENT_MODE_INDEX]))
                    .emailSendAttachmentMode(SendAttachmentMode.fromValue(parts[EMAIL_SEND_ATTACHMENT_MODE_INDEX]))
                    .simpleRegisteredLetterSendAttachmentMode(SendAttachmentMode.fromValue(parts[SIMPLE_REGISTERED_LETTER_SEND_ATTACHMENT_MODE_INDEX]))
                    .build();
        }).toList();
    }
    

}
