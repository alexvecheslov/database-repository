package api.models;

import api.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MakeTransactionRequest extends BaseModel {
    private int senderAccountId;
    private int receiverAccountId;
    private double amount;

}