begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.smoketest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|smoketest
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|Name
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|ParametersFactory
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
name|rest
operator|.
name|ESClientYamlSuiteTestCase
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
name|rest
operator|.
name|RestTestCandidate
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
name|rest
operator|.
name|parser
operator|.
name|RestTestParseException
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_class
DECL|class|SmokeTestDocsIT
specifier|public
class|class
name|SmokeTestDocsIT
extends|extends
name|ESClientYamlSuiteTestCase
block|{
DECL|method|SmokeTestDocsIT
specifier|public
name|SmokeTestDocsIT
parameter_list|(
annotation|@
name|Name
argument_list|(
literal|"yaml"
argument_list|)
name|RestTestCandidate
name|testCandidate
parameter_list|)
block|{
name|super
argument_list|(
name|testCandidate
argument_list|)
expr_stmt|;
block|}
annotation|@
name|ParametersFactory
DECL|method|parameters
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
throws|throws
name|IOException
throws|,
name|RestTestParseException
block|{
return|return
name|ESClientYamlSuiteTestCase
operator|.
name|createParameters
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|afterIfFailed
specifier|protected
name|void
name|afterIfFailed
parameter_list|(
name|List
argument_list|<
name|Throwable
argument_list|>
name|errors
parameter_list|)
block|{
name|super
operator|.
name|afterIfFailed
argument_list|(
name|errors
argument_list|)
expr_stmt|;
name|String
name|name
init|=
name|getTestName
argument_list|()
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
index|[
literal|1
index|]
decl_stmt|;
name|name
operator|=
name|name
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|name
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|name
operator|=
name|name
operator|.
name|replaceAll
argument_list|(
literal|"/([^/]+)$"
argument_list|,
literal|".asciidoc:$1"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|error
argument_list|(
literal|"This failing test was generated by documentation starting at {}. It may include many snippets. "
operator|+
literal|"See docs/README.asciidoc for an explanation of test generation."
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

