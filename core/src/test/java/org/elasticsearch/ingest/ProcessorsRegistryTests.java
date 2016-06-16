begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|service
operator|.
name|ClusterService
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
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|sameInstance
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_class
DECL|class|ProcessorsRegistryTests
specifier|public
class|class
name|ProcessorsRegistryTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBuildProcessorRegistry
specifier|public
name|void
name|testBuildProcessorRegistry
parameter_list|()
block|{
name|ProcessorsRegistry
operator|.
name|Builder
name|builder
init|=
operator|new
name|ProcessorsRegistry
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|TestProcessor
operator|.
name|Factory
name|factory1
init|=
operator|new
name|TestProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|builder
operator|.
name|registerProcessor
argument_list|(
literal|"1"
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
name|factory1
argument_list|)
expr_stmt|;
name|TestProcessor
operator|.
name|Factory
name|factory2
init|=
operator|new
name|TestProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|builder
operator|.
name|registerProcessor
argument_list|(
literal|"2"
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
name|factory2
argument_list|)
expr_stmt|;
name|TestProcessor
operator|.
name|Factory
name|factory3
init|=
operator|new
name|TestProcessor
operator|.
name|Factory
argument_list|()
decl_stmt|;
try|try
block|{
name|builder
operator|.
name|registerProcessor
argument_list|(
literal|"1"
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
name|factory3
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"addProcessor should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Processor factory already registered for name [1]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ProcessorsRegistry
name|registry
init|=
name|builder
operator|.
name|build
argument_list|(
name|mock
argument_list|(
name|ScriptService
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|registry
operator|.
name|getProcessorFactories
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|registry
operator|.
name|getProcessorFactory
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|sameInstance
argument_list|(
name|factory1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|registry
operator|.
name|getProcessorFactory
argument_list|(
literal|"2"
argument_list|)
argument_list|,
name|sameInstance
argument_list|(
name|factory2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

