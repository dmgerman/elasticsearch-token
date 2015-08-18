begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.example
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|example
package|;
end_package

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
name|inject
operator|.
name|Inject
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
name|xcontent
operator|.
name|XContentParser
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
name|xcontent
operator|.
name|yaml
operator|.
name|YamlXContent
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
name|Path
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
operator|.
name|UTF_8
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
operator|.
name|newBufferedReader
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|Streams
operator|.
name|copyToString
import|;
end_import

begin_comment
comment|/**  * Example configuration.  */
end_comment

begin_class
DECL|class|ExamplePluginConfiguration
specifier|public
class|class
name|ExamplePluginConfiguration
block|{
DECL|field|test
specifier|private
name|String
name|test
init|=
literal|"not set in config"
decl_stmt|;
annotation|@
name|Inject
DECL|method|ExamplePluginConfiguration
specifier|public
name|ExamplePluginConfiguration
parameter_list|(
name|Environment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// The directory part of the location matches the artifactId of this plugin
name|Path
name|configFile
init|=
name|env
operator|.
name|configFile
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"jvm-example/example.yaml"
argument_list|)
decl_stmt|;
name|String
name|contents
init|=
name|copyToString
argument_list|(
name|newBufferedReader
argument_list|(
name|configFile
argument_list|,
name|UTF_8
argument_list|)
argument_list|)
decl_stmt|;
name|XContentParser
name|parser
init|=
name|YamlXContent
operator|.
name|yamlXContent
operator|.
name|createParser
argument_list|(
name|contents
argument_list|)
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
assert|assert
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
assert|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"test"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|test
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Unrecognized config key: {}"
argument_list|,
name|currentFieldName
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Unrecognized config key: {}"
argument_list|,
name|currentFieldName
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|getTestConfig
specifier|public
name|String
name|getTestConfig
parameter_list|()
block|{
return|return
name|test
return|;
block|}
block|}
end_class

end_unit

