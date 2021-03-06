begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
import|;
end_import

begin_comment
comment|/**  * Objects that can both render themselves in as json/yaml/etc and can provide a {@link RestStatus} for their response. Usually should be  * implemented by top level responses sent back to users from REST endpoints.  */
end_comment

begin_interface
DECL|interface|StatusToXContentObject
specifier|public
interface|interface
name|StatusToXContentObject
extends|extends
name|ToXContentObject
block|{
comment|/**      * Returns the REST status to make sure it is returned correctly      */
DECL|method|status
name|RestStatus
name|status
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

