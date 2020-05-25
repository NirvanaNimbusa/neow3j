package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StackItemType {

    ANY("Any", 0x00),
    POINTER("Pointer", 0x10),
    BOOLEAN("Boolean", 0x20),
    INTEGER("Integer", 0x21),
    BYTE_STRING("ByteString", 0x28),
    BUFFER("Buffer", 0x30),
    ARRAY("Array", 0x40),
    STRUCT("Struct", 0x41),
    MAP("Map", 0x48),
    INTEROP_INTERFACE("InteropInterface", 0x60);

    private String jsonValue;
    private byte byteValue;

    StackItemType(String jsonValue, int v) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) v;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public static StackItemType valueOf(byte byteValue) {
        for (StackItemType e : StackItemType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

    public static StackItemType fromJsonValue(String jsonValue) {
        for (StackItemType e : StackItemType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

}
