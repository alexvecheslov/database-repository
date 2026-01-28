package models;

import api.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MakeTransactionResponse extends BaseModel {
    private int receiverAccountId;
    private int senderAccountId;
    private double amount;
    private String message;

}

