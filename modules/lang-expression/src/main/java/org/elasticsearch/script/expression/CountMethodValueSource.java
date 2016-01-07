begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.expression
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|expression
package|;
end_package

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
name|Objects
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
name|LeafReaderContext
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
name|queries
operator|.
name|function
operator|.
name|FunctionValues
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
name|queries
operator|.
name|function
operator|.
name|ValueSource
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
name|fielddata
operator|.
name|AtomicFieldData
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
name|fielddata
operator|.
name|AtomicNumericFieldData
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
name|fielddata
operator|.
name|IndexFieldData
import|;
end_import

begin_comment
comment|/**  * A ValueSource to create FunctionValues to get the count of the number of values in a field for a document.  */
end_comment

begin_class
DECL|class|CountMethodValueSource
specifier|public
class|class
name|CountMethodValueSource
extends|extends
name|ValueSource
block|{
DECL|field|fieldData
specifier|protected
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|fieldData
decl_stmt|;
DECL|method|CountMethodValueSource
specifier|protected
name|CountMethodValueSource
parameter_list|(
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|fieldData
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|fieldData
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldData
operator|=
name|fieldData
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
comment|// ValueSource uses a rawtype
DECL|method|getValues
specifier|public
name|FunctionValues
name|getValues
parameter_list|(
name|Map
name|context
parameter_list|,
name|LeafReaderContext
name|leaf
parameter_list|)
throws|throws
name|IOException
block|{
name|AtomicFieldData
name|leafData
init|=
name|fieldData
operator|.
name|load
argument_list|(
name|leaf
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|leafData
operator|instanceof
name|AtomicNumericFieldData
operator|)
assert|;
return|return
operator|new
name|CountMethodFunctionValues
argument_list|(
name|this
argument_list|,
operator|(
name|AtomicNumericFieldData
operator|)
name|leafData
argument_list|)
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|FieldDataValueSource
name|that
init|=
operator|(
name|FieldDataValueSource
operator|)
name|o
decl_stmt|;
return|return
name|fieldData
operator|.
name|equals
argument_list|(
name|that
operator|.
name|fieldData
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
name|fieldData
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"count: field("
operator|+
name|fieldData
operator|.
name|getFieldName
argument_list|()
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

