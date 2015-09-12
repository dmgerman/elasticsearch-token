begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.env
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|env
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
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
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
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
name|endsWith
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
name|notNullValue
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  * Simple unit-tests for Environment.java  */
end_comment

begin_class
DECL|class|EnvironmentTests
specifier|public
class|class
name|EnvironmentTests
extends|extends
name|ESTestCase
block|{
DECL|method|newEnvironment
specifier|public
name|Environment
name|newEnvironment
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|newEnvironment
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
return|;
block|}
DECL|method|newEnvironment
specifier|public
name|Environment
name|newEnvironment
parameter_list|(
name|Settings
name|settings
parameter_list|)
throws|throws
name|IOException
block|{
name|Settings
name|build
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"path.data"
argument_list|,
name|tmpPaths
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
operator|new
name|Environment
argument_list|(
name|build
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|testRepositoryResolution
specifier|public
name|void
name|testRepositoryResolution
parameter_list|()
throws|throws
name|IOException
block|{
name|Environment
name|environment
init|=
name|newEnvironment
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"/test/repos/repo1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"test/repos/repo1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|environment
operator|=
name|newEnvironment
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"path.repo"
argument_list|,
literal|"/test/repos"
argument_list|,
literal|"/another/repos"
argument_list|,
literal|"/test/repos/../other"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"/test/repos/repo1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"test/repos/repo1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"/another/repos/repo1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"/test/repos/../repo1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"/test/repos/../repos/repo1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"/somethingeles/repos/repo1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoFile
argument_list|(
literal|"/test/other/repo"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file:///test/repos/repo1"
argument_list|)
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file:/test/repos/repo1"
argument_list|)
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://test/repos/repo1"
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file:///test/repos/../repo1"
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"http://localhost/test/"
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"jar:file:///test/repos/repo1!/repo/"
argument_list|)
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"jar:file:/test/repos/repo1!/repo/"
argument_list|)
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"jar:file:///test/repos/repo1!/repo/"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|endsWith
argument_list|(
literal|"repo1!/repo/"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"jar:file:///test/repos/../repo1!/repo/"
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|environment
operator|.
name|resolveRepoURL
argument_list|(
operator|new
name|URL
argument_list|(
literal|"jar:http://localhost/test/../repo1?blah!/repo/"
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

