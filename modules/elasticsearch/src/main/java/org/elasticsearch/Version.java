begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimeZone
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|Version
specifier|public
class|class
name|Version
block|{
DECL|field|number
specifier|private
specifier|static
specifier|final
name|String
name|number
decl_stmt|;
DECL|field|date
specifier|private
specifier|static
specifier|final
name|String
name|date
decl_stmt|;
DECL|field|devBuild
specifier|private
specifier|static
specifier|final
name|boolean
name|devBuild
decl_stmt|;
static|static
block|{
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
try|try
block|{
name|InputStream
name|stream
init|=
name|Version
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
literal|"org/elasticsearch/version.properties"
argument_list|)
decl_stmt|;
name|props
operator|.
name|load
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|number
operator|=
name|props
operator|.
name|getProperty
argument_list|(
literal|"number"
argument_list|,
literal|"0.0.0"
argument_list|)
expr_stmt|;
name|SimpleDateFormat
name|sdf
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd'T'HH:mm:ss"
argument_list|)
decl_stmt|;
name|sdf
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"UTC"
argument_list|)
argument_list|)
expr_stmt|;
name|date
operator|=
name|props
operator|.
name|getProperty
argument_list|(
literal|"date"
argument_list|,
name|sdf
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|devBuild
operator|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|props
operator|.
name|getProperty
argument_list|(
literal|"devBuild"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|number
specifier|public
specifier|static
name|String
name|number
parameter_list|()
block|{
return|return
name|number
return|;
block|}
DECL|method|date
specifier|public
specifier|static
name|String
name|date
parameter_list|()
block|{
return|return
name|date
return|;
block|}
DECL|method|devBuild
specifier|public
specifier|static
name|boolean
name|devBuild
parameter_list|()
block|{
return|return
name|devBuild
return|;
block|}
DECL|method|full
specifier|public
specifier|static
name|String
name|full
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"ElasticSearch/"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|number
argument_list|)
expr_stmt|;
if|if
condition|(
name|devBuild
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
operator|.
name|append
argument_list|(
name|date
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"/dev"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

