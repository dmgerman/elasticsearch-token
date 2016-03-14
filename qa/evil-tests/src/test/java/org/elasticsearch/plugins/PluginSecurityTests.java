begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
package|;
end_package

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
name|test
operator|.
name|ESTestCase
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
name|security
operator|.
name|Permission
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PermissionCollection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Permissions
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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

begin_comment
comment|/** Tests plugin manager security check */
end_comment

begin_class
DECL|class|PluginSecurityTests
specifier|public
class|class
name|PluginSecurityTests
extends|extends
name|ESTestCase
block|{
comment|/** Test that we can parse the set of permissions correctly for a simple policy */
DECL|method|testParsePermissions
specifier|public
name|void
name|testParsePermissions
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
literal|"test cannot run with security manager enabled"
argument_list|,
name|System
operator|.
name|getSecurityManager
argument_list|()
operator|==
literal|null
argument_list|)
expr_stmt|;
name|Path
name|scratch
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|testFile
init|=
name|this
operator|.
name|getDataPath
argument_list|(
literal|"security/simple-plugin-security.policy"
argument_list|)
decl_stmt|;
name|Permissions
name|expected
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|RuntimePermission
argument_list|(
literal|"queuePrintJob"
argument_list|)
argument_list|)
expr_stmt|;
name|PermissionCollection
name|actual
init|=
name|PluginSecurity
operator|.
name|parsePermissions
argument_list|(
name|Terminal
operator|.
name|DEFAULT
argument_list|,
name|testFile
argument_list|,
name|scratch
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|/** Test that we can parse the set of permissions correctly for a complex policy */
DECL|method|testParseTwoPermissions
specifier|public
name|void
name|testParseTwoPermissions
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
literal|"test cannot run with security manager enabled"
argument_list|,
name|System
operator|.
name|getSecurityManager
argument_list|()
operator|==
literal|null
argument_list|)
expr_stmt|;
name|Path
name|scratch
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|testFile
init|=
name|this
operator|.
name|getDataPath
argument_list|(
literal|"security/complex-plugin-security.policy"
argument_list|)
decl_stmt|;
name|Permissions
name|expected
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|RuntimePermission
argument_list|(
literal|"getClassLoader"
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|RuntimePermission
argument_list|(
literal|"closeClassLoader"
argument_list|)
argument_list|)
expr_stmt|;
name|PermissionCollection
name|actual
init|=
name|PluginSecurity
operator|.
name|parsePermissions
argument_list|(
name|Terminal
operator|.
name|DEFAULT
argument_list|,
name|testFile
argument_list|,
name|scratch
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|/** Test that we can format some simple permissions properly */
DECL|method|testFormatSimplePermission
specifier|public
name|void
name|testFormatSimplePermission
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
literal|"java.lang.RuntimePermission queuePrintJob"
argument_list|,
name|PluginSecurity
operator|.
name|formatPermission
argument_list|(
operator|new
name|RuntimePermission
argument_list|(
literal|"queuePrintJob"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Test that we can format an unresolved permission properly */
DECL|method|testFormatUnresolvedPermission
specifier|public
name|void
name|testFormatUnresolvedPermission
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
literal|"test cannot run with security manager enabled"
argument_list|,
name|System
operator|.
name|getSecurityManager
argument_list|()
operator|==
literal|null
argument_list|)
expr_stmt|;
name|Path
name|scratch
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|testFile
init|=
name|this
operator|.
name|getDataPath
argument_list|(
literal|"security/unresolved-plugin-security.policy"
argument_list|)
decl_stmt|;
name|PermissionCollection
name|actual
init|=
name|PluginSecurity
operator|.
name|parsePermissions
argument_list|(
name|Terminal
operator|.
name|DEFAULT
argument_list|,
name|testFile
argument_list|,
name|scratch
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Permission
argument_list|>
name|permissions
init|=
name|Collections
operator|.
name|list
argument_list|(
name|actual
operator|.
name|elements
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|permissions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"org.fake.FakePermission fakeName"
argument_list|,
name|PluginSecurity
operator|.
name|formatPermission
argument_list|(
name|permissions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** no guaranteed equals on these classes, we assert they contain the same set */
DECL|method|assertEquals
specifier|private
name|void
name|assertEquals
parameter_list|(
name|PermissionCollection
name|expected
parameter_list|,
name|PermissionCollection
name|actual
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|asSet
argument_list|(
name|Collections
operator|.
name|list
argument_list|(
name|expected
operator|.
name|elements
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|asSet
argument_list|(
name|Collections
operator|.
name|list
argument_list|(
name|actual
operator|.
name|elements
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

