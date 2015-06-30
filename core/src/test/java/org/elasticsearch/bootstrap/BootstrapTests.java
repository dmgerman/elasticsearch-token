begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bootstrap
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
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
name|io
operator|.
name|PathUtils
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
name|ElasticsearchTestCase
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
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|PathMatcher
import|;
end_import

begin_class
DECL|class|BootstrapTests
specifier|public
class|class
name|BootstrapTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testHasLibExtension
specifier|public
name|void
name|testHasLibExtension
parameter_list|()
block|{
name|PathMatcher
name|matcher
init|=
name|PathUtils
operator|.
name|getDefaultFileSystem
argument_list|()
operator|.
name|getPathMatcher
argument_list|(
name|Bootstrap
operator|.
name|PLUGIN_LIB_PATTERN
argument_list|)
decl_stmt|;
name|Path
name|p
init|=
name|PathUtils
operator|.
name|get
argument_list|(
literal|"path"
argument_list|,
literal|"to"
argument_list|,
literal|"plugin.jar"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|matcher
operator|.
name|matches
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
literal|"path"
argument_list|,
literal|"to"
argument_list|,
literal|"plugin.zip"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|matcher
operator|.
name|matches
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
literal|"path"
argument_list|,
literal|"to"
argument_list|,
literal|"plugin.tar.gz"
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|matcher
operator|.
name|matches
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
literal|"path"
argument_list|,
literal|"to"
argument_list|,
literal|"plugin"
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|matcher
operator|.
name|matches
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
