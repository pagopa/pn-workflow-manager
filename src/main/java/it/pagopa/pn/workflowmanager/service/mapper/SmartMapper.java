package it.pagopa.pn.workflowmanager.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class SmartMapper {
    private static final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    static{
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    /*
        Mapping effettuato per la modifica dei timestamp per gli
        elementi di timeline che implementano l'interfaccia ElementTimestampTimelineElementDetails
     */
    public static  <S,T> T mapToClass(S source, Class<T> destinationClass ){
        T result;
        if( source != null) {
            result = modelMapper.map(source, destinationClass );
        } else {
            result = null;
        }
        return result;
    }

    public <S,T> T mapToClassWithObjectMapper(S source, Class<T> destinationClass )  {
        T result;
        try {
            if( source != null) {
                result = objectMapper.readValue(objectMapper.writeValueAsBytes(source), destinationClass);
            } else {
                result = null;
            }
        } catch (IOException e) {
            throw new PnInternalException("Errore durante il mapping del dettaglio", "MAPPING_ERROR", e);
        }

        return result;
    }

}
