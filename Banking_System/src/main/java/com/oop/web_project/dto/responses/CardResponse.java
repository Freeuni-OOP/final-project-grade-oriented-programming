package com.oop.web_project.dto.responses;

import com.oop.web_project.entities.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardResponse {

    private CardType type;

}
