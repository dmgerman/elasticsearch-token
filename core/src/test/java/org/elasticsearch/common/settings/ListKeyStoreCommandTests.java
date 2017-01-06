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
name|ExitCodes
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
name|cli
operator|.
name|UserException
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_class
DECL|class|ListKeyStoreCommandTests
specifier|public
class|class
name|ListKeyStoreCommandTests
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
name|ListKeyStoreCommand
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
DECL|method|testMissing
specifier|public
name|void
name|testMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|UserException
name|e
init|=
name|expectThrows
argument_list|(
name|UserException
operator|.
name|class
argument_list|,
name|this
operator|::
name|execute
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ExitCodes
operator|.
name|DATA_ERROR
argument_list|,
name|e
operator|.
name|exitCode
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"keystore not found"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmpty
specifier|public
name|void
name|testEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|createKeystore
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|execute
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|terminal
operator|.
name|getOutput
argument_list|()
argument_list|,
name|terminal
operator|.
name|getOutput
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testOne
specifier|public
name|void
name|testOne
parameter_list|()
throws|throws
name|Exception
block|{
name|createKeystore
argument_list|(
literal|""
argument_list|,
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|execute
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo\n"
argument_list|,
name|terminal
operator|.
name|getOutput
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiple
specifier|public
name|void
name|testMultiple
parameter_list|()
throws|throws
name|Exception
block|{
name|createKeystore
argument_list|(
literal|""
argument_list|,
literal|"foo"
argument_list|,
literal|"1"
argument_list|,
literal|"baz"
argument_list|,
literal|"2"
argument_list|,
literal|"bar"
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|execute
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar\nbaz\nfoo\n"
argument_list|,
name|terminal
operator|.
name|getOutput
argument_list|()
argument_list|)
expr_stmt|;
comment|// sorted
block|}
block|}
end_class

end_unit

