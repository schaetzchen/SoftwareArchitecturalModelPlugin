package plugindata.entities;

import java.util.ArrayList;
import java.util.List;

public class SoftwareComponent {

    private String name;
    private List<SoftwareInterface> interfaces;

    public SoftwareComponent(String name) {
        this.name = name;
        interfaces = new ArrayList<>();
    }

    public void addInterface(SoftwareInterface i) {
        interfaces.add(i);
    }

    public int getInterfacesCount() {
        return interfaces.size();
    }

    public String getName() {
        return name;
    }

    public List<SoftwareInterface> getInterfaces() {
        return interfaces;
    }
}
