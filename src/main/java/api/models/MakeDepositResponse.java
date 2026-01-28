package api.models;

import api.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MakeDepositResponse extends BaseModel {
    private int id;
    private String accountNumber;
    private double balance;
    private List<Object> transactions;

}