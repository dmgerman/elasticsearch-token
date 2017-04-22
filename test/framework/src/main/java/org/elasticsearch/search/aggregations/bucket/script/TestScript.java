begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|script
package|;
end_package

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
name|elasticsearch
operator|.
name|script
operator|.
name|ExecutableScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|significant
operator|.
name|heuristics
operator|.
name|ScriptHeuristic
import|;
end_import

begin_class
DECL|class|TestScript
specifier|public
specifier|abstract
class|class
name|TestScript
implements|implements
name|ExecutableScript
block|{
DECL|field|_subset_freq
name|ScriptHeuristic
operator|.
name|LongAccessor
name|_subset_freq
decl_stmt|;
DECL|field|_subset_size
name|ScriptHeuristic
operator|.
name|LongAccessor
name|_subset_size
decl_stmt|;
DECL|field|_superset_freq
name|ScriptHeuristic
operator|.
name|LongAccessor
name|_superset_freq
decl_stmt|;
DECL|field|_superset_size
name|ScriptHeuristic
operator|.
name|LongAccessor
name|_superset_size
decl_stmt|;
DECL|method|TestScript
specifier|protected
name|TestScript
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|setNextVar
specifier|public
name|void
name|setNextVar
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
name|name
operator|.
name|equals
argument_list|(
literal|"_subset_freq"
argument_list|)
condition|)
block|{
name|_subset_freq
operator|=
operator|(
name|ScriptHeuristic
operator|.
name|LongAccessor
operator|)
name|value
expr_stmt|;
block|}
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"_subset_size"
argument_list|)
condition|)
block|{
name|_subset_size
operator|=
operator|(
name|ScriptHeuristic
operator|.
name|LongAccessor
operator|)
name|value
expr_stmt|;
block|}
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"_superset_freq"
argument_list|)
condition|)
block|{
name|_superset_freq
operator|=
operator|(
name|ScriptHeuristic
operator|.
name|LongAccessor
operator|)
name|value
expr_stmt|;
block|}
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"_superset_size"
argument_list|)
condition|)
block|{
name|_superset_size
operator|=
operator|(
name|ScriptHeuristic
operator|.
name|LongAccessor
operator|)
name|value
expr_stmt|;
block|}
block|}
DECL|method|checkParams
specifier|protected
specifier|final
name|void
name|checkParams
parameter_list|()
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|_subset_freq
argument_list|,
literal|"_subset_freq"
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|_subset_size
argument_list|,
literal|"_subset_size"
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|_superset_freq
argument_list|,
literal|"_superset_freq"
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|_superset_size
argument_list|,
literal|"_superset_size"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

