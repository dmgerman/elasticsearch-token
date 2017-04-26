begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|PosixFileAttributeView
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
name|attribute
operator|.
name|PosixFilePermission
import|;
end_import

begin_comment
comment|/** Stores the posix attributes for a path and resets them on close. */
end_comment

begin_class
DECL|class|PosixPermissionsResetter
specifier|public
class|class
name|PosixPermissionsResetter
implements|implements
name|AutoCloseable
block|{
DECL|field|attributeView
specifier|private
specifier|final
name|PosixFileAttributeView
name|attributeView
decl_stmt|;
DECL|field|permissions
specifier|private
specifier|final
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
name|permissions
decl_stmt|;
DECL|method|PosixPermissionsResetter
specifier|public
name|PosixPermissionsResetter
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|attributeView
operator|=
name|Files
operator|.
name|getFileAttributeView
argument_list|(
name|path
argument_list|,
name|PosixFileAttributeView
operator|.
name|class
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|attributeView
argument_list|)
expr_stmt|;
name|permissions
operator|=
name|attributeView
operator|.
name|readAttributes
argument_list|()
operator|.
name|permissions
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|attributeView
operator|.
name|setPermissions
argument_list|(
name|permissions
argument_list|)
expr_stmt|;
block|}
DECL|method|setPermissions
specifier|public
name|void
name|setPermissions
parameter_list|(
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
name|newPermissions
parameter_list|)
throws|throws
name|IOException
block|{
name|attributeView
operator|.
name|setPermissions
argument_list|(
name|newPermissions
argument_list|)
expr_stmt|;
block|}
DECL|method|getCopyPermissions
specifier|public
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
name|getCopyPermissions
parameter_list|()
block|{
return|return
name|EnumSet
operator|.
name|copyOf
argument_list|(
name|permissions
argument_list|)
return|;
block|}
block|}
end_class

end_unit

