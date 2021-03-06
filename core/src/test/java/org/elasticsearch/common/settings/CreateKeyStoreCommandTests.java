begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|Command
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|Terminal
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

begin_class
DECL|class|CreateKeyStoreCommandTests
specifier|public
class|class
name|CreateKeyStoreCommandTests
extends|extends
name|KeyStoreCommandTestCase
block|{
annotation|@
name|Override
DECL|method|newCommand
specifier|protected
name|Command
name|newCommand
parameter_list|()
block|{
return|return
operator|new
name|CreateKeyStoreCommand
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Environment
name|createEnv
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|settings
parameter_list|)
block|{
return|return
name|env
return|;
block|}
block|}
return|;
block|}
DECL|method|testPosix
specifier|public
name|void
name|testPosix
parameter_list|()
throws|throws
name|Exception
block|{
name|execute
argument_list|()
expr_stmt|;
name|Path
name|configDir
init|=
name|env
operator|.
name|configFile
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|KeyStoreWrapper
operator|.
name|load
argument_list|(
name|configDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNotPosix
specifier|public
name|void
name|testNotPosix
parameter_list|()
throws|throws
name|Exception
block|{
name|env
operator|=
name|setupEnv
argument_list|(
literal|false
argument_list|,
name|fileSystems
argument_list|)
expr_stmt|;
name|execute
argument_list|()
expr_stmt|;
name|Path
name|configDir
init|=
name|env
operator|.
name|configFile
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|KeyStoreWrapper
operator|.
name|load
argument_list|(
name|configDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testOverwrite
specifier|public
name|void
name|testOverwrite
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|keystoreFile
init|=
name|KeyStoreWrapper
operator|.
name|keystorePath
argument_list|(
name|env
operator|.
name|configFile
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|content
init|=
literal|"not a keystore"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|keystoreFile
argument_list|,
name|content
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|addTextInput
argument_list|(
literal|""
argument_list|)
expr_stmt|;
comment|// default is no
name|execute
argument_list|()
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|content
argument_list|,
name|Files
operator|.
name|readAllBytes
argument_list|(
name|keystoreFile
argument_list|)
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|addTextInput
argument_list|(
literal|"n"
argument_list|)
expr_stmt|;
comment|// explicit no
name|execute
argument_list|()
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|content
argument_list|,
name|Files
operator|.
name|readAllBytes
argument_list|(
name|keystoreFile
argument_list|)
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|addTextInput
argument_list|(
literal|"y"
argument_list|)
expr_stmt|;
name|execute
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|KeyStoreWrapper
operator|.
name|load
argument_list|(
name|env
operator|.
name|configFile
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

