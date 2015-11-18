begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
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
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Represents the data and meta data (like id and type) of a single document that is going to be indexed.  */
end_comment

begin_class
DECL|class|Data
specifier|public
specifier|final
class|class
name|Data
block|{
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
DECL|field|document
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
decl_stmt|;
DECL|field|modified
specifier|private
name|boolean
name|modified
init|=
literal|false
decl_stmt|;
DECL|method|Data
specifier|public
name|Data
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|document
operator|=
name|document
expr_stmt|;
block|}
DECL|method|Data
specifier|public
name|Data
parameter_list|(
name|Data
name|other
parameter_list|)
block|{
name|this
argument_list|(
name|other
operator|.
name|index
argument_list|,
name|other
operator|.
name|type
argument_list|,
name|other
operator|.
name|id
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|other
operator|.
name|document
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the value contained in the document for the provided path      * @param path The path within the document in dot-notation      * @param clazz The expected class of the field value      * @return the value for the provided path if existing, null otherwise      * @throws IllegalArgumentException if the field is present but is not of the type provided as argument.      */
DECL|method|getPropertyValue
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|getPropertyValue
parameter_list|(
name|String
name|path
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
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
assert|assert
name|pathElements
operator|.
name|length
operator|>
literal|0
assert|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|innerMap
init|=
name|getParent
argument_list|(
name|pathElements
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerMap
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|leafKey
init|=
name|pathElements
index|[
name|pathElements
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|Object
name|property
init|=
name|innerMap
operator|.
name|get
argument_list|(
name|leafKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|property
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
name|clazz
operator|.
name|isInstance
argument_list|(
name|property
argument_list|)
condition|)
block|{
return|return
name|clazz
operator|.
name|cast
argument_list|(
name|property
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field ["
operator|+
name|path
operator|+
literal|"] of type ["
operator|+
name|property
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"] cannot be cast to ["
operator|+
name|clazz
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|/**      * Checks whether the document contains a value for the provided path      * @param path The path within the document in dot-notation      * @return true if the document contains a value for the property, false otherwise      */
DECL|method|hasPropertyValue
specifier|public
name|boolean
name|hasPropertyValue
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
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
assert|assert
name|pathElements
operator|.
name|length
operator|>
literal|0
assert|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|innerMap
init|=
name|getParent
argument_list|(
name|pathElements
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerMap
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|leafKey
init|=
name|pathElements
index|[
name|pathElements
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
return|return
name|innerMap
operator|.
name|containsKey
argument_list|(
name|leafKey
argument_list|)
return|;
block|}
comment|/**      * Removes the property identified by the provided path      * @param path the path of the property to be removed      */
DECL|method|removeProperty
specifier|public
name|void
name|removeProperty
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
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
assert|assert
name|pathElements
operator|.
name|length
operator|>
literal|0
assert|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|parent
init|=
name|getParent
argument_list|(
name|pathElements
argument_list|)
decl_stmt|;
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|String
name|leafKey
init|=
name|pathElements
index|[
name|pathElements
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|parent
operator|.
name|remove
argument_list|(
name|leafKey
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getParent
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getParent
parameter_list|(
name|String
index|[]
name|pathElements
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|innerMap
init|=
name|document
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|pathElements
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|obj
init|=
name|innerMap
operator|.
name|get
argument_list|(
name|pathElements
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|obj
operator|instanceof
name|Map
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|stringObjectMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|obj
decl_stmt|;
name|innerMap
operator|=
name|stringObjectMap
expr_stmt|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
return|return
name|innerMap
return|;
block|}
comment|/**      * Sets the provided value to the provided path in the document.      * Any non existing path element will be created.      * @param path The path within the document in dot-notation      * @param value The value to put in for the path key      */
DECL|method|setPropertyValue
specifier|public
name|void
name|setPropertyValue
parameter_list|(
name|String
name|path
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"cannot add null or empty field"
argument_list|)
throw|;
block|}
name|modified
operator|=
literal|true
expr_stmt|;
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
assert|assert
name|pathElements
operator|.
name|length
operator|>
literal|0
assert|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|inner
init|=
name|document
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|pathElements
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|String
name|pathElement
init|=
name|pathElements
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|inner
operator|.
name|containsKey
argument_list|(
name|pathElement
argument_list|)
condition|)
block|{
name|Object
name|object
init|=
name|inner
operator|.
name|get
argument_list|(
name|pathElement
argument_list|)
decl_stmt|;
if|if
condition|(
name|object
operator|instanceof
name|Map
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|stringObjectMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|object
decl_stmt|;
name|inner
operator|=
name|stringObjectMap
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|object
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"cannot add field to null parent, ["
operator|+
name|Map
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"] expected instead."
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"cannot add field to parent ["
operator|+
name|pathElement
operator|+
literal|"] of type ["
operator|+
name|object
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"], ["
operator|+
name|Map
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"] expected instead."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|newInnerMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|inner
operator|.
name|put
argument_list|(
name|pathElement
argument_list|,
name|newInnerMap
argument_list|)
expr_stmt|;
name|inner
operator|=
name|newInnerMap
expr_stmt|;
block|}
block|}
name|String
name|leafKey
init|=
name|pathElements
index|[
name|pathElements
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|inner
operator|.
name|put
argument_list|(
name|leafKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|getDocument
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getDocument
parameter_list|()
block|{
return|return
name|document
return|;
block|}
DECL|method|isModified
specifier|public
name|boolean
name|isModified
parameter_list|()
block|{
return|return
name|modified
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Data
name|other
init|=
operator|(
name|Data
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|document
argument_list|,
name|other
operator|.
name|document
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|index
argument_list|,
name|other
operator|.
name|index
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|type
argument_list|,
name|other
operator|.
name|type
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|id
argument_list|,
name|other
operator|.
name|id
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|document
argument_list|)
return|;
block|}
block|}
end_class

end_unit

