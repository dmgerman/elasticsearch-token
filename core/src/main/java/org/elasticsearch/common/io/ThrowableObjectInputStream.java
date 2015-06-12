begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.io
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
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
name|Classes
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ThrowableObjectInputStream
specifier|public
class|class
name|ThrowableObjectInputStream
extends|extends
name|ObjectInputStream
block|{
DECL|field|classLoader
specifier|private
specifier|final
name|ClassLoader
name|classLoader
decl_stmt|;
DECL|method|ThrowableObjectInputStream
specifier|public
name|ThrowableObjectInputStream
parameter_list|(
name|InputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|in
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|ThrowableObjectInputStream
specifier|public
name|ThrowableObjectInputStream
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|ClassLoader
name|classLoader
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|classLoader
operator|=
name|classLoader
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readStreamHeader
specifier|protected
name|void
name|readStreamHeader
parameter_list|()
throws|throws
name|IOException
throws|,
name|StreamCorruptedException
block|{
name|int
name|version
init|=
name|readByte
argument_list|()
operator|&
literal|0xFF
decl_stmt|;
if|if
condition|(
name|version
operator|!=
name|STREAM_VERSION
condition|)
block|{
throw|throw
operator|new
name|StreamCorruptedException
argument_list|(
literal|"Unsupported version: "
operator|+
name|version
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|readClassDescriptor
specifier|protected
name|ObjectStreamClass
name|readClassDescriptor
parameter_list|()
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|int
name|type
init|=
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|ThrowableObjectOutputStream
operator|.
name|TYPE_EXCEPTION
case|:
return|return
name|ObjectStreamClass
operator|.
name|lookup
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
return|;
case|case
name|ThrowableObjectOutputStream
operator|.
name|TYPE_STACKTRACEELEMENT
case|:
return|return
name|ObjectStreamClass
operator|.
name|lookup
argument_list|(
name|StackTraceElement
operator|.
name|class
argument_list|)
return|;
case|case
name|ThrowableObjectOutputStream
operator|.
name|TYPE_FAT_DESCRIPTOR
case|:
return|return
name|super
operator|.
name|readClassDescriptor
argument_list|()
return|;
case|case
name|ThrowableObjectOutputStream
operator|.
name|TYPE_THIN_DESCRIPTOR
case|:
name|String
name|className
init|=
name|readUTF
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|loadClass
argument_list|(
name|className
argument_list|)
decl_stmt|;
return|return
name|ObjectStreamClass
operator|.
name|lookup
argument_list|(
name|clazz
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|StreamCorruptedException
argument_list|(
literal|"Unexpected class descriptor type: "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|resolveClass
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|resolveClass
parameter_list|(
name|ObjectStreamClass
name|desc
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|String
name|className
init|=
name|desc
operator|.
name|getName
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|loadClass
argument_list|(
name|className
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|ex
parameter_list|)
block|{
return|return
name|super
operator|.
name|resolveClass
argument_list|(
name|desc
argument_list|)
return|;
block|}
block|}
DECL|method|loadClass
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|loadClass
parameter_list|(
name|String
name|className
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
decl_stmt|;
name|ClassLoader
name|classLoader
init|=
name|this
operator|.
name|classLoader
decl_stmt|;
if|if
condition|(
name|classLoader
operator|==
literal|null
condition|)
block|{
name|classLoader
operator|=
name|Classes
operator|.
name|getDefaultClassLoader
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|classLoader
operator|!=
literal|null
condition|)
block|{
name|clazz
operator|=
name|classLoader
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|clazz
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
return|return
name|clazz
return|;
block|}
block|}
end_class

end_unit
