package telegram.bot.rules;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SlotMachineData implements Serializable {
    public final String machineName;
    private final List<List<String>> strips;
    public final List<List<Integer>> lines;
}
