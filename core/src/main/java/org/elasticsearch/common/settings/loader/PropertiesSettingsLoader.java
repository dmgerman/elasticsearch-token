begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.settings.loader
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|loader
package|;
end_package

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
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|FastStringReader
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
name|StreamInput
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
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_comment
comment|/**  * Settings loader that loads (parses) the settings in a properties format.  */
end_comment

begin_class
DECL|class|PropertiesSettingsLoader
specifier|public
class|class
name|PropertiesSettingsLoader
implements|implements
name|SettingsLoader
block|{
annotation|@
name|Override
DECL|method|load
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|load
parameter_list|(
name|String
name|source
parameter_list|)
throws|throws
name|IOException
block|{
name|Properties
name|props
init|=
operator|new
name|NoDuplicatesProperties
argument_list|()
decl_stmt|;
name|FastStringReader
name|reader
init|=
operator|new
name|FastStringReader
argument_list|(
name|source
argument_list|)
decl_stmt|;
try|try
block|{
name|props
operator|.
name|load
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|result
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
name|entry
range|:
name|props
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|result
operator|.
name|put
argument_list|(
operator|(
name|String
operator|)
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|String
operator|)
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|load
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|load
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
throws|throws
name|IOException
block|{
name|Properties
name|props
init|=
operator|new
name|NoDuplicatesProperties
argument_list|()
decl_stmt|;
name|StreamInput
name|stream
init|=
name|StreamInput
operator|.
name|wrap
argument_list|(
name|source
argument_list|)
decl_stmt|;
try|try
block|{
name|props
operator|.
name|load
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|result
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
name|entry
range|:
name|props
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|result
operator|.
name|put
argument_list|(
operator|(
name|String
operator|)
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|String
operator|)
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NoDuplicatesProperties
class|class
name|NoDuplicatesProperties
extends|extends
name|Properties
block|{
annotation|@
name|Override
DECL|method|put
specifier|public
specifier|synchronized
name|Object
name|put
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|Object
name|previousValue
init|=
name|super
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|previousValue
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"duplicate settings key [{}] found, previous value [{}], current value [{}]"
argument_list|,
name|key
argument_list|,
name|previousValue
argument_list|,
name|value
argument_list|)
throw|;
block|}
return|return
name|previousValue
return|;
block|}
block|}
block|}
end_class

end_unit

