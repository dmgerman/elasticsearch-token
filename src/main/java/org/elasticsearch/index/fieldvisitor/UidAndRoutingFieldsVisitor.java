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
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|internal
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
name|internal
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|UidAndRoutingFieldsVisitor
specifier|public
class|class
name|UidAndRoutingFieldsVisitor
extends|extends
name|FieldsVisitor
block|{
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
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
name|RoutingFieldMapper
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
return|return
name|Status
operator|.
name|YES
return|;
block|}
elseif|else
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
return|return
name|Status
operator|.
name|YES
return|;
block|}
return|return
name|uid
operator|!=
literal|null
operator|&&
name|routing
operator|!=
literal|null
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
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|RoutingFieldMapper
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
name|routing
operator|=
name|value
expr_stmt|;
block|}
else|else
block|{
name|super
operator|.
name|stringField
argument_list|(
name|fieldInfo
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|routing
specifier|public
name|String
name|routing
parameter_list|()
block|{
return|return
name|routing
return|;
block|}
block|}
end_class

end_unit

