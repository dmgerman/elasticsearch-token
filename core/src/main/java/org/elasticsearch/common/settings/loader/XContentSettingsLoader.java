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

begin_comment
comment|/**  * Settings loader that loads (parses) the settings in a xcontent format by flattening them  * into a map.  */
end_comment

begin_class
DECL|class|XContentSettingsLoader
specifier|public
specifier|abstract
class|class
name|XContentSettingsLoader
implements|implements
name|SettingsLoader
block|{
DECL|method|contentType
specifier|public
specifier|abstract
name|XContentType
name|contentType
parameter_list|()
function_decl|;
DECL|field|guardAgainstNullValuedSettings
specifier|private
specifier|final
name|boolean
name|guardAgainstNullValuedSettings
decl_stmt|;
DECL|method|XContentSettingsLoader
name|XContentSettingsLoader
parameter_list|(
name|boolean
name|guardAgainstNullValuedSettings
parameter_list|)
block|{
name|this
operator|.
name|guardAgainstNullValuedSettings
operator|=
name|guardAgainstNullValuedSettings
expr_stmt|;
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
name|String
name|source
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|contentType
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
init|)
block|{
return|return
name|load
argument_list|(
name|parser
argument_list|)
return|;
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
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|contentType
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
init|)
block|{
return|return
name|load
argument_list|(
name|parser
argument_list|)
return|;
block|}
block|}
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
name|XContentParser
name|jp
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|settings
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|path
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
name|jp
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
return|return
name|settings
return|;
block|}
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
literal|"malformed, expected settings to start with 'object', instead was [{}]"
argument_list|,
name|token
argument_list|)
throw|;
block|}
name|serializeObject
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|jp
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// ensure we reached the end of the stream
name|XContentParser
operator|.
name|Token
name|lastToken
init|=
literal|null
decl_stmt|;
try|try
block|{
while|while
condition|(
operator|!
name|jp
operator|.
name|isClosed
argument_list|()
operator|&&
operator|(
name|lastToken
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|==
literal|null
condition|)
empty_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"malformed, expected end of settings but encountered additional content starting at line number: [{}], column number: [{}]"
argument_list|,
name|e
argument_list|,
name|jp
operator|.
name|getTokenLocation
argument_list|()
operator|.
name|lineNumber
argument_list|,
name|jp
operator|.
name|getTokenLocation
argument_list|()
operator|.
name|columnNumber
argument_list|)
throw|;
block|}
if|if
condition|(
name|lastToken
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"malformed, expected end of settings but encountered additional content starting at line number: [{}], column number: [{}]"
argument_list|,
name|jp
operator|.
name|getTokenLocation
argument_list|()
operator|.
name|lineNumber
argument_list|,
name|jp
operator|.
name|getTokenLocation
argument_list|()
operator|.
name|columnNumber
argument_list|)
throw|;
block|}
return|return
name|settings
return|;
block|}
DECL|method|serializeObject
specifier|private
name|void
name|serializeObject
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|settings
parameter_list|,
name|StringBuilder
name|sb
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|path
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|String
name|objFieldName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|objFieldName
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|add
argument_list|(
name|objFieldName
argument_list|)
expr_stmt|;
block|}
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
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
name|START_OBJECT
condition|)
block|{
name|serializeObject
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|parser
argument_list|,
name|currentFieldName
argument_list|)
expr_stmt|;
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
name|START_ARRAY
condition|)
block|{
name|serializeArray
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|parser
argument_list|,
name|currentFieldName
argument_list|)
expr_stmt|;
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
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NULL
condition|)
block|{
name|serializeValue
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|parser
argument_list|,
name|currentFieldName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serializeValue
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|parser
argument_list|,
name|currentFieldName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|objFieldName
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|remove
argument_list|(
name|path
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|serializeArray
specifier|private
name|void
name|serializeArray
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|settings
parameter_list|,
name|StringBuilder
name|sb
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|path
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|int
name|counter
init|=
literal|0
decl_stmt|;
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
name|END_ARRAY
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
name|START_OBJECT
condition|)
block|{
name|serializeObject
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|parser
argument_list|,
name|fieldName
operator|+
literal|'.'
operator|+
operator|(
name|counter
operator|++
operator|)
argument_list|)
expr_stmt|;
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
name|START_ARRAY
condition|)
block|{
name|serializeArray
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|parser
argument_list|,
name|fieldName
operator|+
literal|'.'
operator|+
operator|(
name|counter
operator|++
operator|)
argument_list|)
expr_stmt|;
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
condition|)
block|{
name|fieldName
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
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NULL
condition|)
block|{
name|serializeValue
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|parser
argument_list|,
name|fieldName
operator|+
literal|'.'
operator|+
operator|(
name|counter
operator|++
operator|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// ignore
block|}
else|else
block|{
name|serializeValue
argument_list|(
name|settings
argument_list|,
name|sb
argument_list|,
name|path
argument_list|,
name|parser
argument_list|,
name|fieldName
operator|+
literal|'.'
operator|+
operator|(
name|counter
operator|++
operator|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|serializeValue
specifier|private
name|void
name|serializeValue
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|settings
parameter_list|,
name|StringBuilder
name|sb
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|path
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|boolean
name|isNull
parameter_list|)
throws|throws
name|IOException
block|{
name|sb
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|pathEle
range|:
name|path
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|pathEle
argument_list|)
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|String
name|key
init|=
name|sb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|currentValue
init|=
name|isNull
condition|?
literal|null
else|:
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
name|settings
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"duplicate settings key [{}] found at line number [{}], column number [{}], previous value [{}], current value [{}]"
argument_list|,
name|key
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
operator|.
name|lineNumber
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
operator|.
name|columnNumber
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|,
name|currentValue
argument_list|)
throw|;
block|}
if|if
condition|(
name|guardAgainstNullValuedSettings
operator|&&
name|currentValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"null-valued setting found for key [{}] found at line number [{}], column number [{}]"
argument_list|,
name|key
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
operator|.
name|lineNumber
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
operator|.
name|columnNumber
argument_list|)
throw|;
block|}
name|settings
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|currentValue
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

