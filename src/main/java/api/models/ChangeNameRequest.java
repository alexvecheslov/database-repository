package models;

import api.models.BaseModel;
import api.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeNameRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z]{1,10} [A-Za-z]{1,10}$")
    private String name;

}