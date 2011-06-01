begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  */
end_comment

begin_package
DECL|package|org.elasticsearch.thrift
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|thrift
package|;
end_package

begin_enum
DECL|enum|Method
specifier|public
enum|enum
name|Method
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
block|{
DECL|enum constant|GET
name|GET
argument_list|(
literal|0
argument_list|)
block|,
DECL|enum constant|PUT
name|PUT
argument_list|(
literal|1
argument_list|)
block|,
DECL|enum constant|POST
name|POST
argument_list|(
literal|2
argument_list|)
block|,
DECL|enum constant|DELETE
name|DELETE
argument_list|(
literal|3
argument_list|)
block|,
DECL|enum constant|HEAD
name|HEAD
argument_list|(
literal|4
argument_list|)
block|,
DECL|enum constant|OPTIONS
name|OPTIONS
argument_list|(
literal|5
argument_list|)
block|;
DECL|field|value
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
DECL|method|Method
specifier|private
name|Method
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**      * Get the integer value of this enum value, as defined in the Thrift IDL.      */
DECL|method|getValue
specifier|public
name|int
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|/**      * Find a the enum type by its integer value, as defined in the Thrift IDL.      *      * @return null if the value is not found.      */
DECL|method|findByValue
specifier|public
specifier|static
name|Method
name|findByValue
parameter_list|(
name|int
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|value
condition|)
block|{
case|case
literal|0
case|:
return|return
name|GET
return|;
case|case
literal|1
case|:
return|return
name|PUT
return|;
case|case
literal|2
case|:
return|return
name|POST
return|;
case|case
literal|3
case|:
return|return
name|DELETE
return|;
case|case
literal|4
case|:
return|return
name|HEAD
return|;
case|case
literal|5
case|:
return|return
name|OPTIONS
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
end_enum

end_unit

