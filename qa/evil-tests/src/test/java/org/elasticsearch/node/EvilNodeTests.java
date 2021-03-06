begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|node
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Constants
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
name|UUIDs
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
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
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
name|nio
operator|.
name|file
operator|.
name|Files
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

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verifyNoMoreInteractions
import|;
end_import

begin_class
DECL|class|EvilNodeTests
specifier|public
class|class
name|EvilNodeTests
extends|extends
name|ESTestCase
block|{
DECL|method|testDefaultPathDataIncludedInPathData
specifier|public
name|void
name|testDefaultPathDataIncludedInPathData
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|zero
init|=
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|one
init|=
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
decl_stmt|;
comment|// creating hard links to directories is okay on macOS so we exercise it here
specifier|final
name|int
name|random
decl_stmt|;
if|if
condition|(
name|Constants
operator|.
name|MAC_OS_X
condition|)
block|{
name|random
operator|=
name|randomFrom
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|random
operator|=
name|randomFrom
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Path
name|defaultPathData
decl_stmt|;
specifier|final
name|Path
name|choice
init|=
name|randomFrom
argument_list|(
name|zero
argument_list|,
name|one
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|random
condition|)
block|{
case|case
literal|0
case|:
name|defaultPathData
operator|=
name|choice
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|defaultPathData
operator|=
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"link"
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createSymbolicLink
argument_list|(
name|defaultPathData
argument_list|,
name|choice
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|defaultPathData
operator|=
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"link"
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createLink
argument_list|(
name|defaultPathData
argument_list|,
name|choice
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|random
argument_list|)
argument_list|)
throw|;
block|}
specifier|final
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
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
name|put
argument_list|(
literal|"path.data.0"
argument_list|,
name|zero
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.data.1"
argument_list|,
name|one
argument_list|)
operator|.
name|put
argument_list|(
literal|"default.path.data"
argument_list|,
name|defaultPathData
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
init|(
name|NodeEnvironment
name|nodeEnv
init|=
operator|new
name|NodeEnvironment
argument_list|(
name|settings
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
init|)
block|{
specifier|final
name|Path
name|defaultPathDataWithNodesAndId
init|=
name|defaultPathData
operator|.
name|resolve
argument_list|(
literal|"nodes/0"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|defaultPathDataWithNodesAndId
argument_list|)
expr_stmt|;
specifier|final
name|NodeEnvironment
operator|.
name|NodePath
name|defaultNodePath
init|=
operator|new
name|NodeEnvironment
operator|.
name|NodePath
argument_list|(
name|defaultPathDataWithNodesAndId
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|defaultNodePath
operator|.
name|indicesPath
operator|.
name|resolve
argument_list|(
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Logger
name|mock
init|=
name|mock
argument_list|(
name|Logger
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// nothing should happen here
name|Node
operator|.
name|checkForIndexDataInDefaultPathData
argument_list|(
name|settings
argument_list|,
name|nodeEnv
argument_list|,
name|mock
argument_list|)
expr_stmt|;
name|verifyNoMoreInteractions
argument_list|(
name|mock
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

