package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.cms.AttributeTable;

import java.util.Map;

/**
 * Basic generator that just returns a preconstructed attribute table
 */
public class SimpleAttributeTableGenerator
    implements CMSAttributeTableGenerator
{
    private final AttributeTable attributes;

    public SimpleAttributeTableGenerator(
        AttributeTable attributes)
    {
        this.attributes = attributes;
    }

    public AttributeTable getAttributes(Map parameters)
    {
        return attributes;
    }
}
