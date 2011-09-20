begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|support
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|XContentMapValues
specifier|public
class|class
name|XContentMapValues
block|{
comment|/**      * Extracts raw values (string, int, and so on) based on the path provided returning all of them      * as a single list.      */
DECL|method|extractRawValues
specifier|public
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|extractRawValues
parameter_list|(
name|String
name|path
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|String
index|[]
name|pathElements
init|=
name|Strings
operator|.
name|splitStringToArray
argument_list|(
name|path
argument_list|,
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|pathElements
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|values
return|;
block|}
name|extractRawValues
argument_list|(
name|values
argument_list|,
name|map
argument_list|,
name|pathElements
argument_list|,
literal|0
argument_list|)
expr_stmt|;
return|return
name|values
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|extractRawValues
specifier|private
specifier|static
name|void
name|extractRawValues
parameter_list|(
name|List
name|values
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|part
parameter_list|,
name|String
index|[]
name|pathElements
parameter_list|,
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|index
operator|==
name|pathElements
operator|.
name|length
condition|)
block|{
return|return;
block|}
name|String
name|currentPath
init|=
name|pathElements
index|[
name|index
index|]
decl_stmt|;
name|Object
name|currentValue
init|=
name|part
operator|.
name|get
argument_list|(
name|currentPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentValue
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|currentValue
operator|instanceof
name|Map
condition|)
block|{
name|extractRawValues
argument_list|(
name|values
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|currentValue
argument_list|,
name|pathElements
argument_list|,
name|index
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentValue
operator|instanceof
name|List
condition|)
block|{
name|extractRawValues
argument_list|(
name|values
argument_list|,
operator|(
name|List
operator|)
name|currentValue
argument_list|,
name|pathElements
argument_list|,
name|index
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|values
operator|.
name|add
argument_list|(
name|currentValue
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|extractRawValues
specifier|private
specifier|static
name|void
name|extractRawValues
parameter_list|(
name|List
name|values
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|part
parameter_list|,
name|String
index|[]
name|pathElements
parameter_list|,
name|int
name|index
parameter_list|)
block|{
for|for
control|(
name|Object
name|value
range|:
name|part
control|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
name|extractRawValues
argument_list|(
name|values
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|value
argument_list|,
name|pathElements
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|List
condition|)
block|{
name|extractRawValues
argument_list|(
name|values
argument_list|,
operator|(
name|List
operator|)
name|value
argument_list|,
name|pathElements
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|extractValue
specifier|public
specifier|static
name|Object
name|extractValue
parameter_list|(
name|String
name|path
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|)
block|{
name|String
index|[]
name|pathElements
init|=
name|Strings
operator|.
name|splitStringToArray
argument_list|(
name|path
argument_list|,
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|pathElements
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|extractValue
argument_list|(
name|pathElements
argument_list|,
literal|0
argument_list|,
name|map
argument_list|)
return|;
block|}
DECL|method|extractValue
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
specifier|private
specifier|static
name|Object
name|extractValue
parameter_list|(
name|String
index|[]
name|pathElements
parameter_list|,
name|int
name|index
parameter_list|,
name|Object
name|currentValue
parameter_list|)
block|{
if|if
condition|(
name|index
operator|==
name|pathElements
operator|.
name|length
condition|)
block|{
return|return
name|currentValue
return|;
block|}
if|if
condition|(
name|currentValue
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|currentValue
operator|instanceof
name|Map
condition|)
block|{
name|Map
name|map
init|=
operator|(
name|Map
operator|)
name|currentValue
decl_stmt|;
return|return
name|extractValue
argument_list|(
name|pathElements
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|map
operator|.
name|get
argument_list|(
name|pathElements
index|[
name|index
index|]
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|currentValue
operator|instanceof
name|List
condition|)
block|{
name|List
name|valueList
init|=
operator|(
name|List
operator|)
name|currentValue
decl_stmt|;
name|List
name|newList
init|=
operator|new
name|ArrayList
argument_list|(
name|valueList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|valueList
control|)
block|{
name|Object
name|listValue
init|=
name|extractValue
argument_list|(
name|pathElements
argument_list|,
name|index
argument_list|,
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|listValue
operator|!=
literal|null
condition|)
block|{
name|newList
operator|.
name|add
argument_list|(
name|listValue
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|newList
return|;
block|}
return|return
literal|null
return|;
block|}
DECL|method|isObject
specifier|public
specifier|static
name|boolean
name|isObject
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
return|return
name|node
operator|instanceof
name|Map
return|;
block|}
DECL|method|isArray
specifier|public
specifier|static
name|boolean
name|isArray
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
return|return
name|node
operator|instanceof
name|List
return|;
block|}
DECL|method|nodeStringValue
specifier|public
specifier|static
name|String
name|nodeStringValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|node
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|nodeFloatValue
specifier|public
specifier|static
name|float
name|nodeFloatValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|float
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeFloatValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeFloatValue
specifier|public
specifier|static
name|float
name|nodeFloatValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|floatValue
argument_list|()
return|;
block|}
return|return
name|Float
operator|.
name|parseFloat
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeDoubleValue
specifier|public
specifier|static
name|double
name|nodeDoubleValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|double
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeDoubleValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeDoubleValue
specifier|public
specifier|static
name|double
name|nodeDoubleValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeIntegerValue
specifier|public
specifier|static
name|int
name|nodeIntegerValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|intValue
argument_list|()
return|;
block|}
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeIntegerValue
specifier|public
specifier|static
name|int
name|nodeIntegerValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|int
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|intValue
argument_list|()
return|;
block|}
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeShortValue
specifier|public
specifier|static
name|short
name|nodeShortValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|short
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeShortValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeShortValue
specifier|public
specifier|static
name|short
name|nodeShortValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|shortValue
argument_list|()
return|;
block|}
return|return
name|Short
operator|.
name|parseShort
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeByteValue
specifier|public
specifier|static
name|byte
name|nodeByteValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|byte
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeByteValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeByteValue
specifier|public
specifier|static
name|byte
name|nodeByteValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|byteValue
argument_list|()
return|;
block|}
return|return
name|Byte
operator|.
name|parseByte
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeLongValue
specifier|public
specifier|static
name|long
name|nodeLongValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|long
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeLongValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeLongValue
specifier|public
specifier|static
name|long
name|nodeLongValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeBooleanValue
specifier|public
specifier|static
name|boolean
name|nodeBooleanValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|boolean
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeBooleanValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeBooleanValue
specifier|public
specifier|static
name|boolean
name|nodeBooleanValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Boolean
condition|)
block|{
return|return
operator|(
name|Boolean
operator|)
name|node
return|;
block|}
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|intValue
argument_list|()
operator|!=
literal|0
return|;
block|}
name|String
name|value
init|=
name|node
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
operator|!
operator|(
name|value
operator|.
name|equals
argument_list|(
literal|"false"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"0"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"off"
argument_list|)
operator|)
return|;
block|}
DECL|method|nodeTimeValue
specifier|public
specifier|static
name|TimeValue
name|nodeTimeValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|TimeValue
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeTimeValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeTimeValue
specifier|public
specifier|static
name|TimeValue
name|nodeTimeValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
return|;
block|}
return|return
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

