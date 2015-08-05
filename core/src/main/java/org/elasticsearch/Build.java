begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch
package|package
name|org
operator|.
name|elasticsearch
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
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|format
operator|.
name|ISODateTimeFormat
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|Build
specifier|public
class|class
name|Build
block|{
DECL|field|CURRENT
specifier|public
specifier|static
specifier|final
name|Build
name|CURRENT
decl_stmt|;
static|static
block|{
name|String
name|hash
init|=
literal|"NA"
decl_stmt|;
name|String
name|hashShort
init|=
literal|"NA"
decl_stmt|;
name|String
name|timestamp
init|=
literal|"NA"
decl_stmt|;
try|try
init|(
name|InputStream
name|is
init|=
name|Build
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/es-build.properties"
argument_list|)
init|)
block|{
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|load
argument_list|(
name|is
argument_list|)
expr_stmt|;
name|hash
operator|=
name|props
operator|.
name|getProperty
argument_list|(
literal|"hash"
argument_list|,
name|hash
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|hash
operator|.
name|equals
argument_list|(
literal|"NA"
argument_list|)
condition|)
block|{
name|hashShort
operator|=
name|hash
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|7
argument_list|)
expr_stmt|;
block|}
name|String
name|gitTimestampRaw
init|=
name|props
operator|.
name|getProperty
argument_list|(
literal|"timestamp"
argument_list|)
decl_stmt|;
if|if
condition|(
name|gitTimestampRaw
operator|!=
literal|null
condition|)
block|{
name|timestamp
operator|=
name|ISODateTimeFormat
operator|.
name|dateTimeNoMillis
argument_list|()
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|print
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|gitTimestampRaw
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// just ignore...
block|}
name|CURRENT
operator|=
operator|new
name|Build
argument_list|(
name|hash
argument_list|,
name|hashShort
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
DECL|field|hash
specifier|private
name|String
name|hash
decl_stmt|;
DECL|field|hashShort
specifier|private
name|String
name|hashShort
decl_stmt|;
DECL|field|timestamp
specifier|private
name|String
name|timestamp
decl_stmt|;
DECL|method|Build
name|Build
parameter_list|(
name|String
name|hash
parameter_list|,
name|String
name|hashShort
parameter_list|,
name|String
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|hash
operator|=
name|hash
expr_stmt|;
name|this
operator|.
name|hashShort
operator|=
name|hashShort
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
DECL|method|hash
specifier|public
name|String
name|hash
parameter_list|()
block|{
return|return
name|hash
return|;
block|}
DECL|method|hashShort
specifier|public
name|String
name|hashShort
parameter_list|()
block|{
return|return
name|hashShort
return|;
block|}
DECL|method|timestamp
specifier|public
name|String
name|timestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
DECL|method|readBuild
specifier|public
specifier|static
name|Build
name|readBuild
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|hash
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|String
name|hashShort
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|String
name|timestamp
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
return|return
operator|new
name|Build
argument_list|(
name|hash
argument_list|,
name|hashShort
argument_list|,
name|timestamp
argument_list|)
return|;
block|}
DECL|method|writeBuild
specifier|public
specifier|static
name|void
name|writeBuild
parameter_list|(
name|Build
name|build
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|build
operator|.
name|hash
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|build
operator|.
name|hashShort
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|build
operator|.
name|timestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|hash
operator|+
literal|"]["
operator|+
name|timestamp
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

