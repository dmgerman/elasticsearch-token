begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.selector
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|selector
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
name|document
operator|.
name|FieldSelectorResult
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
name|lucene
operator|.
name|document
operator|.
name|ResetFieldSelector
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
name|SourceFieldMapper
import|;
end_import

begin_comment
comment|/**  * A field selector that loads all fields except the source field.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AllButSourceFieldSelector
specifier|public
class|class
name|AllButSourceFieldSelector
implements|implements
name|ResetFieldSelector
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|AllButSourceFieldSelector
name|INSTANCE
init|=
operator|new
name|AllButSourceFieldSelector
argument_list|()
decl_stmt|;
DECL|method|accept
annotation|@
name|Override
specifier|public
name|FieldSelectorResult
name|accept
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
if|if
condition|(
name|SourceFieldMapper
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
return|return
name|FieldSelectorResult
operator|.
name|NO_LOAD
return|;
block|}
return|return
name|FieldSelectorResult
operator|.
name|LOAD
return|;
block|}
DECL|method|reset
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{     }
block|}
end_class

end_unit

