begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.mustache
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|mustache
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|io
operator|.
name|JsonStringEncoder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|DefaultMustacheFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|MustacheException
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
name|Writer
import|;
end_import

begin_comment
comment|/**  * A MustacheFactory that does simple JSON escaping.  */
end_comment

begin_class
DECL|class|JsonEscapingMustacheFactory
specifier|final
class|class
name|JsonEscapingMustacheFactory
extends|extends
name|DefaultMustacheFactory
block|{
annotation|@
name|Override
DECL|method|encode
specifier|public
name|void
name|encode
parameter_list|(
name|String
name|value
parameter_list|,
name|Writer
name|writer
parameter_list|)
block|{
try|try
block|{
name|writer
operator|.
name|write
argument_list|(
name|JsonStringEncoder
operator|.
name|getInstance
argument_list|()
operator|.
name|quoteAsString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MustacheException
argument_list|(
literal|"Failed to encode value: "
operator|+
name|value
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

