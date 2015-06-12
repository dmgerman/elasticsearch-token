begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
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
name|SortedDocValues
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

begin_comment
comment|/**  * Specialization of {@link AtomicFieldData} for parent/child mappings.  */
end_comment

begin_interface
DECL|interface|AtomicParentChildFieldData
specifier|public
interface|interface
name|AtomicParentChildFieldData
extends|extends
name|AtomicFieldData
block|{
comment|/**      * Return the set of types there is a mapping for.      */
DECL|method|types
name|Set
argument_list|<
name|String
argument_list|>
name|types
parameter_list|()
function_decl|;
comment|/**      * Return the mapping for the given type. The returned      * {@link SortedDocValues} will map doc IDs to the identifier of their      * parent.      */
DECL|method|getOrdinalsValues
name|SortedDocValues
name|getOrdinalsValues
parameter_list|(
name|String
name|type
parameter_list|)
function_decl|;
block|}
end_interface

end_unit
