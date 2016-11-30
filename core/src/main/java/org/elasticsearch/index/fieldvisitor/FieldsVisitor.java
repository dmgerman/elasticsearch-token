begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fieldvisitor
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fieldvisitor
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|FieldInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|StoredFieldVisitor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
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
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MappedFieldType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|ParentFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|RoutingFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|SourceFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|Uid
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|UidFieldMapper
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|set
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_comment
comment|/**  * Base {@link StoredFieldVisitor} that retrieves all non-redundant metadata.  */
end_comment

begin_class
DECL|class|FieldsVisitor
specifier|public
class|class
name|FieldsVisitor
extends|extends
name|StoredFieldVisitor
block|{
DECL|field|BASE_REQUIRED_FIELDS
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|BASE_REQUIRED_FIELDS
init|=
name|unmodifiableSet
argument_list|(
name|newHashSet
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|RoutingFieldMapper
operator|.
name|NAME
argument_list|,
name|ParentFieldMapper
operator|.
name|NAME
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|loadSource
specifier|private
specifier|final
name|boolean
name|loadSource
decl_stmt|;
DECL|field|requiredFields
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|requiredFields
decl_stmt|;
DECL|field|source
specifier|protected
name|BytesReference
name|source
decl_stmt|;
DECL|field|uid
specifier|protected
name|Uid
name|uid
decl_stmt|;
DECL|field|fieldsValues
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|fieldsValues
decl_stmt|;
DECL|method|FieldsVisitor
specifier|public
name|FieldsVisitor
parameter_list|(
name|boolean
name|loadSource
parameter_list|)
block|{
name|this
operator|.
name|loadSource
operator|=
name|loadSource
expr_stmt|;
name|requiredFields
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsField
specifier|public
name|Status
name|needsField
parameter_list|(
name|FieldInfo
name|fieldInfo
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|requiredFields
operator|.
name|remove
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|)
condition|)
block|{
return|return
name|Status
operator|.
name|YES
return|;
block|}
comment|// All these fields are single-valued so we can stop when the set is
comment|// empty
return|return
name|requiredFields
operator|.
name|isEmpty
argument_list|()
condition|?
name|Status
operator|.
name|STOP
else|:
name|Status
operator|.
name|NO
return|;
block|}
DECL|method|postProcess
specifier|public
name|void
name|postProcess
parameter_list|(
name|MapperService
name|mapperService
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|entry
range|:
name|fields
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|MappedFieldType
name|fieldType
init|=
name|mapperService
operator|.
name|fullName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Field ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"] exists in the index but not in mappings"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|fieldValues
init|=
name|entry
operator|.
name|getValue
argument_list|()
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
name|fieldValues
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldValues
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|fieldType
operator|.
name|valueForDisplay
argument_list|(
name|fieldValues
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|binaryField
specifier|public
name|void
name|binaryField
parameter_list|(
name|FieldInfo
name|fieldInfo
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|SourceFieldMapper
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|)
condition|)
block|{
name|source
operator|=
operator|new
name|BytesArray
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addValue
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|stringField
specifier|public
name|void
name|stringField
parameter_list|(
name|FieldInfo
name|fieldInfo
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|value
init|=
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
if|if
condition|(
name|UidFieldMapper
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|)
condition|)
block|{
name|uid
operator|=
name|Uid
operator|.
name|createUid
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addValue
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|intField
specifier|public
name|void
name|intField
parameter_list|(
name|FieldInfo
name|fieldInfo
parameter_list|,
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|addValue
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|longField
specifier|public
name|void
name|longField
parameter_list|(
name|FieldInfo
name|fieldInfo
parameter_list|,
name|long
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|addValue
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|floatField
specifier|public
name|void
name|floatField
parameter_list|(
name|FieldInfo
name|fieldInfo
parameter_list|,
name|float
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|addValue
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doubleField
specifier|public
name|void
name|doubleField
parameter_list|(
name|FieldInfo
name|fieldInfo
parameter_list|,
name|double
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|addValue
argument_list|(
name|fieldInfo
operator|.
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
DECL|method|source
specifier|public
name|BytesReference
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|uid
specifier|public
name|Uid
name|uid
parameter_list|()
block|{
return|return
name|uid
return|;
block|}
DECL|method|routing
specifier|public
name|String
name|routing
parameter_list|()
block|{
if|if
condition|(
name|fieldsValues
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|fieldsValues
operator|.
name|get
argument_list|(
name|RoutingFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|==
literal|null
operator|||
name|values
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
assert|assert
name|values
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
return|return
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|fields
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|fields
parameter_list|()
block|{
return|return
name|fieldsValues
operator|!=
literal|null
condition|?
name|fieldsValues
else|:
name|emptyMap
argument_list|()
return|;
block|}
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{
if|if
condition|(
name|fieldsValues
operator|!=
literal|null
condition|)
name|fieldsValues
operator|.
name|clear
argument_list|()
expr_stmt|;
name|source
operator|=
literal|null
expr_stmt|;
name|uid
operator|=
literal|null
expr_stmt|;
name|requiredFields
operator|.
name|addAll
argument_list|(
name|BASE_REQUIRED_FIELDS
argument_list|)
expr_stmt|;
if|if
condition|(
name|loadSource
condition|)
block|{
name|requiredFields
operator|.
name|add
argument_list|(
name|SourceFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|addValue
name|void
name|addValue
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|fieldsValues
operator|==
literal|null
condition|)
block|{
name|fieldsValues
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|fieldsValues
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|==
literal|null
condition|)
block|{
name|values
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|fieldsValues
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

