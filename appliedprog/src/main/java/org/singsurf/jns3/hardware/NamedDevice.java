package org.singsurf.jns3.hardware;

public abstract class NamedDevice {
    private String name;

    public NamedDevice(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}