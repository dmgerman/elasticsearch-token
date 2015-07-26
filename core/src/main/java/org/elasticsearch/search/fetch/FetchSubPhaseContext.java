begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
package|;
end_package

begin_class
DECL|class|FetchSubPhaseContext
specifier|public
class|class
name|FetchSubPhaseContext
block|{
DECL|field|hitExecutionNeeded
specifier|private
name|boolean
name|hitExecutionNeeded
init|=
literal|false
decl_stmt|;
DECL|method|setHitExecutionNeeded
name|void
name|setHitExecutionNeeded
parameter_list|(
name|boolean
name|hitExecutionNeeded
parameter_list|)
block|{
name|this
operator|.
name|hitExecutionNeeded
operator|=
name|hitExecutionNeeded
expr_stmt|;
block|}
DECL|method|hitExecutionNeeded
specifier|public
name|boolean
name|hitExecutionNeeded
parameter_list|()
block|{
return|return
name|hitExecutionNeeded
return|;
block|}
block|}
end_class

end_unit

