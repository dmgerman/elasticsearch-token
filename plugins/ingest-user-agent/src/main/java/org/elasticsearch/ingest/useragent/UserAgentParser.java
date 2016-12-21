begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.useragent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|useragent
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
name|xcontent
operator|.
name|NamedXContentRegistry
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
name|XContentFactory
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
name|XContentType
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
name|ArrayList
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
name|List
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
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_class
DECL|class|UserAgentParser
specifier|final
class|class
name|UserAgentParser
block|{
DECL|field|cache
specifier|private
specifier|final
name|UserAgentCache
name|cache
decl_stmt|;
DECL|field|uaPatterns
specifier|private
specifier|final
name|List
argument_list|<
name|UserAgentSubpattern
argument_list|>
name|uaPatterns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|osPatterns
specifier|private
specifier|final
name|List
argument_list|<
name|UserAgentSubpattern
argument_list|>
name|osPatterns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|devicePatterns
specifier|private
specifier|final
name|List
argument_list|<
name|UserAgentSubpattern
argument_list|>
name|devicePatterns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|UserAgentParser
specifier|public
name|UserAgentParser
parameter_list|(
name|String
name|name
parameter_list|,
name|InputStream
name|regexStream
parameter_list|,
name|UserAgentCache
name|cache
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|cache
operator|=
name|cache
expr_stmt|;
try|try
block|{
name|init
argument_list|(
name|regexStream
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
name|ElasticsearchParseException
argument_list|(
literal|"error parsing regular expression file"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|init
specifier|private
name|void
name|init
parameter_list|(
name|InputStream
name|regexStream
parameter_list|)
throws|throws
name|IOException
block|{
comment|// EMPTY is safe here because we don't use namedObject
name|XContentParser
name|yamlParser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|YAML
argument_list|)
operator|.
name|createParser
argument_list|(
name|NamedXContentRegistry
operator|.
name|EMPTY
argument_list|,
name|regexStream
argument_list|)
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|token
operator|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
for|for
control|(
init|;
name|token
operator|!=
literal|null
condition|;
name|token
operator|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
control|)
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
operator|&&
name|yamlParser
operator|.
name|currentName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"user_agent_parsers"
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|parserConfigurations
init|=
name|readParserConfigurations
argument_list|(
name|yamlParser
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
range|:
name|parserConfigurations
control|)
block|{
name|uaPatterns
operator|.
name|add
argument_list|(
operator|new
name|UserAgentSubpattern
argument_list|(
name|compilePattern
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"regex"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"regex_flag"
argument_list|)
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"family_replacement"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"v1_replacement"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"v2_replacement"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"v3_replacement"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"v4_replacement"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
operator|&&
name|yamlParser
operator|.
name|currentName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"os_parsers"
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|parserConfigurations
init|=
name|readParserConfigurations
argument_list|(
name|yamlParser
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
range|:
name|parserConfigurations
control|)
block|{
name|osPatterns
operator|.
name|add
argument_list|(
operator|new
name|UserAgentSubpattern
argument_list|(
name|compilePattern
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"regex"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"regex_flag"
argument_list|)
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"os_replacement"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"os_v1_replacement"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"os_v2_replacement"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"os_v3_replacement"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"os_v4_replacement"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
operator|&&
name|yamlParser
operator|.
name|currentName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"device_parsers"
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|parserConfigurations
init|=
name|readParserConfigurations
argument_list|(
name|yamlParser
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
range|:
name|parserConfigurations
control|)
block|{
name|devicePatterns
operator|.
name|add
argument_list|(
operator|new
name|UserAgentSubpattern
argument_list|(
name|compilePattern
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"regex"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"regex_flag"
argument_list|)
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"device_replacement"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|uaPatterns
operator|.
name|isEmpty
argument_list|()
operator|&&
name|osPatterns
operator|.
name|isEmpty
argument_list|()
operator|&&
name|devicePatterns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"not a valid regular expression file"
argument_list|)
throw|;
block|}
block|}
DECL|method|compilePattern
specifier|private
name|Pattern
name|compilePattern
parameter_list|(
name|String
name|regex
parameter_list|,
name|String
name|regex_flag
parameter_list|)
block|{
comment|// Only flag present in the current default regexes.yaml
if|if
condition|(
name|regex_flag
operator|!=
literal|null
operator|&&
name|regex_flag
operator|.
name|equals
argument_list|(
literal|"i"
argument_list|)
condition|)
block|{
return|return
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
argument_list|)
return|;
block|}
block|}
DECL|method|readParserConfigurations
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|readParserConfigurations
parameter_list|(
name|XContentParser
name|yamlParser
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|patternList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"malformed regular expression file, should continue with 'array' after 'object'"
argument_list|)
throw|;
block|}
name|token
operator|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"malformed regular expression file, expecting 'object'"
argument_list|)
throw|;
block|}
while|while
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|token
operator|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"malformed regular expression file, should continue with 'field_name' after 'array'"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|regexMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
init|;
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|;
name|token
operator|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
control|)
block|{
name|String
name|fieldName
init|=
name|yamlParser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|token
operator|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|String
name|fieldValue
init|=
name|yamlParser
operator|.
name|text
argument_list|()
decl_stmt|;
name|regexMap
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
expr_stmt|;
block|}
name|patternList
operator|.
name|add
argument_list|(
name|regexMap
argument_list|)
expr_stmt|;
name|token
operator|=
name|yamlParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
return|return
name|patternList
return|;
block|}
DECL|method|getUaPatterns
name|List
argument_list|<
name|UserAgentSubpattern
argument_list|>
name|getUaPatterns
parameter_list|()
block|{
return|return
name|uaPatterns
return|;
block|}
DECL|method|getOsPatterns
name|List
argument_list|<
name|UserAgentSubpattern
argument_list|>
name|getOsPatterns
parameter_list|()
block|{
return|return
name|osPatterns
return|;
block|}
DECL|method|getDevicePatterns
name|List
argument_list|<
name|UserAgentSubpattern
argument_list|>
name|getDevicePatterns
parameter_list|()
block|{
return|return
name|devicePatterns
return|;
block|}
DECL|method|getName
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|parse
specifier|public
name|Details
name|parse
parameter_list|(
name|String
name|agentString
parameter_list|)
block|{
name|Details
name|details
init|=
name|cache
operator|.
name|get
argument_list|(
name|name
argument_list|,
name|agentString
argument_list|)
decl_stmt|;
empty_stmt|;
if|if
condition|(
name|details
operator|==
literal|null
condition|)
block|{
name|VersionedName
name|userAgent
init|=
name|findMatch
argument_list|(
name|uaPatterns
argument_list|,
name|agentString
argument_list|)
decl_stmt|;
name|VersionedName
name|operatingSystem
init|=
name|findMatch
argument_list|(
name|osPatterns
argument_list|,
name|agentString
argument_list|)
decl_stmt|;
name|VersionedName
name|device
init|=
name|findMatch
argument_list|(
name|devicePatterns
argument_list|,
name|agentString
argument_list|)
decl_stmt|;
name|details
operator|=
operator|new
name|Details
argument_list|(
name|userAgent
argument_list|,
name|operatingSystem
argument_list|,
name|device
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|agentString
argument_list|,
name|details
argument_list|)
expr_stmt|;
block|}
return|return
name|details
return|;
block|}
DECL|method|findMatch
specifier|private
name|VersionedName
name|findMatch
parameter_list|(
name|List
argument_list|<
name|UserAgentSubpattern
argument_list|>
name|possiblePatterns
parameter_list|,
name|String
name|agentString
parameter_list|)
block|{
name|VersionedName
name|name
decl_stmt|;
for|for
control|(
name|UserAgentSubpattern
name|pattern
range|:
name|possiblePatterns
control|)
block|{
name|name
operator|=
name|pattern
operator|.
name|match
argument_list|(
name|agentString
argument_list|)
expr_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
return|return
name|name
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
DECL|class|Details
specifier|static
specifier|final
class|class
name|Details
block|{
DECL|field|userAgent
specifier|public
specifier|final
name|VersionedName
name|userAgent
decl_stmt|;
DECL|field|operatingSystem
specifier|public
specifier|final
name|VersionedName
name|operatingSystem
decl_stmt|;
DECL|field|device
specifier|public
specifier|final
name|VersionedName
name|device
decl_stmt|;
DECL|method|Details
specifier|public
name|Details
parameter_list|(
name|VersionedName
name|userAgent
parameter_list|,
name|VersionedName
name|operatingSystem
parameter_list|,
name|VersionedName
name|device
parameter_list|)
block|{
name|this
operator|.
name|userAgent
operator|=
name|userAgent
expr_stmt|;
name|this
operator|.
name|operatingSystem
operator|=
name|operatingSystem
expr_stmt|;
name|this
operator|.
name|device
operator|=
name|device
expr_stmt|;
block|}
block|}
DECL|class|VersionedName
specifier|static
specifier|final
class|class
name|VersionedName
block|{
DECL|field|name
specifier|public
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|major
specifier|public
specifier|final
name|String
name|major
decl_stmt|;
DECL|field|minor
specifier|public
specifier|final
name|String
name|minor
decl_stmt|;
DECL|field|patch
specifier|public
specifier|final
name|String
name|patch
decl_stmt|;
DECL|field|build
specifier|public
specifier|final
name|String
name|build
decl_stmt|;
DECL|method|VersionedName
specifier|public
name|VersionedName
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|major
parameter_list|,
name|String
name|minor
parameter_list|,
name|String
name|patch
parameter_list|,
name|String
name|build
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|major
operator|=
name|major
expr_stmt|;
name|this
operator|.
name|minor
operator|=
name|minor
expr_stmt|;
name|this
operator|.
name|patch
operator|=
name|patch
expr_stmt|;
name|this
operator|.
name|build
operator|=
name|build
expr_stmt|;
block|}
block|}
comment|/**      * One of: user agent, operating system, device      */
DECL|class|UserAgentSubpattern
specifier|static
specifier|final
class|class
name|UserAgentSubpattern
block|{
DECL|field|pattern
specifier|private
specifier|final
name|Pattern
name|pattern
decl_stmt|;
DECL|field|nameReplacement
DECL|field|v1Replacement
DECL|field|v2Replacement
DECL|field|v3Replacement
DECL|field|v4Replacement
specifier|private
specifier|final
name|String
name|nameReplacement
decl_stmt|,
name|v1Replacement
decl_stmt|,
name|v2Replacement
decl_stmt|,
name|v3Replacement
decl_stmt|,
name|v4Replacement
decl_stmt|;
DECL|method|UserAgentSubpattern
specifier|public
name|UserAgentSubpattern
parameter_list|(
name|Pattern
name|pattern
parameter_list|,
name|String
name|nameReplacement
parameter_list|,
name|String
name|v1Replacement
parameter_list|,
name|String
name|v2Replacement
parameter_list|,
name|String
name|v3Replacement
parameter_list|,
name|String
name|v4Replacement
parameter_list|)
block|{
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
name|this
operator|.
name|nameReplacement
operator|=
name|nameReplacement
expr_stmt|;
name|this
operator|.
name|v1Replacement
operator|=
name|v1Replacement
expr_stmt|;
name|this
operator|.
name|v2Replacement
operator|=
name|v2Replacement
expr_stmt|;
name|this
operator|.
name|v3Replacement
operator|=
name|v3Replacement
expr_stmt|;
name|this
operator|.
name|v4Replacement
operator|=
name|v4Replacement
expr_stmt|;
block|}
DECL|method|match
specifier|public
name|VersionedName
name|match
parameter_list|(
name|String
name|agentString
parameter_list|)
block|{
name|String
name|name
init|=
literal|null
decl_stmt|,
name|major
init|=
literal|null
decl_stmt|,
name|minor
init|=
literal|null
decl_stmt|,
name|patch
init|=
literal|null
decl_stmt|,
name|build
init|=
literal|null
decl_stmt|;
name|Matcher
name|matcher
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|agentString
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|groupCount
init|=
name|matcher
operator|.
name|groupCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|nameReplacement
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|nameReplacement
operator|.
name|contains
argument_list|(
literal|"$1"
argument_list|)
operator|&&
name|groupCount
operator|>=
literal|1
operator|&&
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|nameReplacement
operator|.
name|replaceFirst
argument_list|(
literal|"\\$1"
argument_list|,
name|Matcher
operator|.
name|quoteReplacement
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|name
operator|=
name|nameReplacement
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|groupCount
operator|>=
literal|1
condition|)
block|{
name|name
operator|=
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|v1Replacement
operator|!=
literal|null
condition|)
block|{
name|major
operator|=
name|v1Replacement
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|groupCount
operator|>=
literal|2
condition|)
block|{
name|major
operator|=
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|v2Replacement
operator|!=
literal|null
condition|)
block|{
name|minor
operator|=
name|v2Replacement
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|groupCount
operator|>=
literal|3
condition|)
block|{
name|minor
operator|=
name|matcher
operator|.
name|group
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|v3Replacement
operator|!=
literal|null
condition|)
block|{
name|patch
operator|=
name|v3Replacement
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|groupCount
operator|>=
literal|4
condition|)
block|{
name|patch
operator|=
name|matcher
operator|.
name|group
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|v4Replacement
operator|!=
literal|null
condition|)
block|{
name|build
operator|=
name|v4Replacement
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|groupCount
operator|>=
literal|5
condition|)
block|{
name|build
operator|=
name|matcher
operator|.
name|group
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
return|return
name|name
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|VersionedName
argument_list|(
name|name
argument_list|,
name|major
argument_list|,
name|minor
argument_list|,
name|patch
argument_list|,
name|build
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

