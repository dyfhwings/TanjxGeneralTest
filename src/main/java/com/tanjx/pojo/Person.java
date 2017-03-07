package com.tanjx.pojo;

public class Person
{
    @Override
    public String toString()
    {
        return "Person [id=" + this.id + ", name=" + this.name + "]";
    }

    private String id;
    private String name;

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
