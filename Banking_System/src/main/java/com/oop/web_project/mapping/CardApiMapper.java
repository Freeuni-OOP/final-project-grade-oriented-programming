package com.oop.web_project.mapping;

import com.oop.web_project.dto.responses.CardResponse;
import com.oop.web_project.entities.Card;
import org.springframework.stereotype.Component;

@Component
public class CardApiMapper {

    public CardResponse toCardResponse(Card card){
        CardResponse response = new CardResponse();
        return null;
    }
}
