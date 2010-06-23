begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.cache.field.data
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|field
operator|.
name|data
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
name|IndexReader
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
name|component
operator|.
name|CloseableComponent
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
name|field
operator|.
name|data
operator|.
name|FieldData
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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_interface
DECL|interface|FieldDataCache
specifier|public
interface|interface
name|FieldDataCache
extends|extends
name|CloseableComponent
block|{
DECL|method|cache
parameter_list|<
name|T
extends|extends
name|FieldData
parameter_list|>
name|T
name|cache
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|cache
name|FieldData
name|cache
parameter_list|(
name|FieldData
operator|.
name|Type
name|type
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|type
name|String
name|type
parameter_list|()
function_decl|;
DECL|method|clear
name|void
name|clear
parameter_list|()
function_decl|;
DECL|method|clear
name|void
name|clear
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
function_decl|;
DECL|method|clearUnreferenced
name|void
name|clearUnreferenced
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

