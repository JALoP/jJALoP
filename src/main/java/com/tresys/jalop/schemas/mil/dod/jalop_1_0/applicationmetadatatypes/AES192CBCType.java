//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.03.06 at 10:05:13 AM EST 
//


package com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AES192_CBC_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AES192_CBC_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Key" type="{http://www.dod.mil/jalop-1.0/applicationMetadataTypes}Base64Binary24Bytes" minOccurs="0"/>
 *         &lt;element name="IV" type="{http://www.dod.mil/jalop-1.0/applicationMetadataTypes}Base64Binary16Bytes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AES192_CBC_Type", propOrder = {
    "key",
    "iv"
})
public class AES192CBCType {

    @XmlElement(name = "Key")
    protected byte[] key;
    @XmlElement(name = "IV")
    protected byte[] iv;

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setKey(byte[] value) {
        this.key = ((byte[]) value);
    }

    /**
     * Gets the value of the iv property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getIV() {
        return iv;
    }

    /**
     * Sets the value of the iv property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setIV(byte[] value) {
        this.iv = ((byte[]) value);
    }

}
