begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
package|;
end_package

begin_class
DECL|class|ClusterManager
specifier|public
class|class
name|ClusterManager
block|{
DECL|field|cluster
specifier|private
specifier|static
name|TestCluster
name|cluster
decl_stmt|;
DECL|method|accquireCluster
specifier|public
specifier|synchronized
specifier|static
name|TestCluster
name|accquireCluster
parameter_list|()
block|{
if|if
condition|(
name|cluster
operator|==
literal|null
condition|)
block|{
name|cluster
operator|=
operator|new
name|TestCluster
argument_list|()
expr_stmt|;
block|}
name|cluster
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|cluster
return|;
block|}
DECL|method|releaseCluster
specifier|public
specifier|static
specifier|synchronized
name|void
name|releaseCluster
parameter_list|()
block|{
comment|// doNothing
block|}
block|}
end_class

end_unit

