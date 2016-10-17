begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|ExceptionsHelper
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
name|Arrays
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
DECL|class|ConfigurationUtils
specifier|public
specifier|final
class|class
name|ConfigurationUtils
block|{
DECL|field|TAG_KEY
specifier|public
specifier|static
specifier|final
name|String
name|TAG_KEY
init|=
literal|"tag"
decl_stmt|;
DECL|method|ConfigurationUtils
specifier|private
name|ConfigurationUtils
parameter_list|()
block|{     }
comment|/**      * Returns and removes the specified optional property from the specified configuration map.      *      * If the property value isn't of type string a {@link ElasticsearchParseException} is thrown.      */
DECL|method|readOptionalStringProperty
specifier|public
specifier|static
name|String
name|readOptionalStringProperty
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
return|return
name|readString
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**      * Returns and removes the specified property from the specified configuration map.      *      * If the property value isn't of type string an {@link ElasticsearchParseException} is thrown.      * If the property is missing an {@link ElasticsearchParseException} is thrown      */
DECL|method|readStringProperty
specifier|public
specifier|static
name|String
name|readStringProperty
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|)
block|{
return|return
name|readStringProperty
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|configuration
argument_list|,
name|propertyName
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Returns and removes the specified property from the specified configuration map.      *      * If the property value isn't of type string a {@link ElasticsearchParseException} is thrown.      * If the property is missing and no default value has been specified a {@link ElasticsearchParseException} is thrown      */
DECL|method|readStringProperty
specifier|public
specifier|static
name|String
name|readStringProperty
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
operator|&&
name|defaultValue
operator|!=
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"required property is missing"
argument_list|)
throw|;
block|}
return|return
name|readString
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|readString
specifier|private
specifier|static
name|String
name|readString
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
return|return
operator|(
name|String
operator|)
name|value
return|;
block|}
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"property isn't a string, but of type ["
operator|+
name|value
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|method|readBooleanProperty
specifier|public
specifier|static
name|Boolean
name|readBooleanProperty
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|boolean
name|defaultValue
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
else|else
block|{
return|return
name|readBoolean
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
operator|.
name|booleanValue
argument_list|()
return|;
block|}
block|}
DECL|method|readBoolean
specifier|private
specifier|static
name|Boolean
name|readBoolean
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|value
operator|instanceof
name|Boolean
condition|)
block|{
return|return
operator|(
name|Boolean
operator|)
name|value
return|;
block|}
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"property isn't a boolean, but of type ["
operator|+
name|value
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|/**      * Returns and removes the specified property from the specified configuration map.      *      * If the property value isn't of type int a {@link ElasticsearchParseException} is thrown.      * If the property is missing an {@link ElasticsearchParseException} is thrown      */
DECL|method|readIntProperty
specifier|public
specifier|static
name|Integer
name|readIntProperty
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|Integer
name|defaultValue
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
try|try
block|{
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"property cannot be converted to an int ["
operator|+
name|value
operator|.
name|toString
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Returns and removes the specified property of type list from the specified configuration map.      *      * If the property value isn't of type list an {@link ElasticsearchParseException} is thrown.      */
DECL|method|readOptionalList
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|readOptionalList
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|readList
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**      * Returns and removes the specified property of type list from the specified configuration map.      *      * If the property value isn't of type list an {@link ElasticsearchParseException} is thrown.      * If the property is missing an {@link ElasticsearchParseException} is thrown      */
DECL|method|readList
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|readList
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"required property is missing"
argument_list|)
throw|;
block|}
return|return
name|readList
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|readList
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|readList
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|List
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|T
argument_list|>
name|stringList
init|=
operator|(
name|List
argument_list|<
name|T
argument_list|>
operator|)
name|value
decl_stmt|;
return|return
name|stringList
return|;
block|}
else|else
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"property isn't a list, but of type ["
operator|+
name|value
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Returns and removes the specified property of type map from the specified configuration map.      *      * If the property value isn't of type map an {@link ElasticsearchParseException} is thrown.      * If the property is missing an {@link ElasticsearchParseException} is thrown      */
DECL|method|readMap
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Map
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|readMap
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"required property is missing"
argument_list|)
throw|;
block|}
return|return
name|readMap
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**      * Returns and removes the specified property of type map from the specified configuration map.      *      * If the property value isn't of type map an {@link ElasticsearchParseException} is thrown.      */
DECL|method|readOptionalMap
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Map
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|readOptionalMap
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|readMap
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|readMap
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Map
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|readMap
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|map
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
operator|)
name|value
decl_stmt|;
return|return
name|map
return|;
block|}
else|else
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"property isn't a map, but of type ["
operator|+
name|value
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Returns and removes the specified property as an {@link Object} from the specified configuration map.      */
DECL|method|readObject
specifier|public
specifier|static
name|Object
name|readObject
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|configuration
parameter_list|,
name|String
name|propertyName
parameter_list|)
block|{
name|Object
name|value
init|=
name|configuration
operator|.
name|remove
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
literal|"required property is missing"
argument_list|)
throw|;
block|}
return|return
name|value
return|;
block|}
DECL|method|newConfigurationException
specifier|public
specifier|static
name|ElasticsearchException
name|newConfigurationException
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|String
name|msg
decl_stmt|;
if|if
condition|(
name|propertyName
operator|==
literal|null
condition|)
block|{
name|msg
operator|=
name|reason
expr_stmt|;
block|}
else|else
block|{
name|msg
operator|=
literal|"["
operator|+
name|propertyName
operator|+
literal|"] "
operator|+
name|reason
expr_stmt|;
block|}
name|ElasticsearchParseException
name|exception
init|=
operator|new
name|ElasticsearchParseException
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|addHeadersToException
argument_list|(
name|exception
argument_list|,
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|)
expr_stmt|;
return|return
name|exception
return|;
block|}
DECL|method|newConfigurationException
specifier|public
specifier|static
name|ElasticsearchException
name|newConfigurationException
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|Exception
name|cause
parameter_list|)
block|{
name|ElasticsearchException
name|exception
init|=
name|ExceptionsHelper
operator|.
name|convertToElastic
argument_list|(
name|cause
argument_list|)
decl_stmt|;
name|addHeadersToException
argument_list|(
name|exception
argument_list|,
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|)
expr_stmt|;
return|return
name|exception
return|;
block|}
DECL|method|readProcessorConfigs
specifier|public
specifier|static
name|List
argument_list|<
name|Processor
argument_list|>
name|readProcessorConfigs
parameter_list|(
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|>
name|processorConfigs
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorFactories
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Processor
argument_list|>
name|processors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|processorConfigs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|processorConfigWithKey
range|:
name|processorConfigs
control|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|entry
range|:
name|processorConfigWithKey
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|processors
operator|.
name|add
argument_list|(
name|readProcessor
argument_list|(
name|processorFactories
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|processors
return|;
block|}
DECL|method|compileTemplate
specifier|public
specifier|static
name|TemplateService
operator|.
name|Template
name|compileTemplate
parameter_list|(
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|String
name|propertyName
parameter_list|,
name|String
name|propertyValue
parameter_list|,
name|TemplateService
name|templateService
parameter_list|)
block|{
try|try
block|{
return|return
name|templateService
operator|.
name|compile
argument_list|(
name|propertyValue
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|ConfigurationUtils
operator|.
name|newConfigurationException
argument_list|(
name|processorType
argument_list|,
name|processorTag
argument_list|,
name|propertyName
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|addHeadersToException
specifier|private
specifier|static
name|void
name|addHeadersToException
parameter_list|(
name|ElasticsearchException
name|exception
parameter_list|,
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|String
name|propertyName
parameter_list|)
block|{
if|if
condition|(
name|processorType
operator|!=
literal|null
condition|)
block|{
name|exception
operator|.
name|addHeader
argument_list|(
literal|"processor_type"
argument_list|,
name|processorType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|processorTag
operator|!=
literal|null
condition|)
block|{
name|exception
operator|.
name|addHeader
argument_list|(
literal|"processor_tag"
argument_list|,
name|processorTag
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|propertyName
operator|!=
literal|null
condition|)
block|{
name|exception
operator|.
name|addHeader
argument_list|(
literal|"property_name"
argument_list|,
name|propertyName
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|readProcessor
specifier|public
specifier|static
name|Processor
name|readProcessor
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorFactories
parameter_list|,
name|String
name|type
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|Exception
block|{
name|Processor
operator|.
name|Factory
name|factory
init|=
name|processorFactories
operator|.
name|get
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|factory
operator|!=
literal|null
condition|)
block|{
name|boolean
name|ignoreFailure
init|=
name|ConfigurationUtils
operator|.
name|readBooleanProperty
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
literal|"ignore_failure"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|>
name|onFailureProcessorConfigs
init|=
name|ConfigurationUtils
operator|.
name|readOptionalList
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
name|Pipeline
operator|.
name|ON_FAILURE_KEY
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Processor
argument_list|>
name|onFailureProcessors
init|=
name|readProcessorConfigs
argument_list|(
name|onFailureProcessorConfigs
argument_list|,
name|processorFactories
argument_list|)
decl_stmt|;
name|String
name|tag
init|=
name|ConfigurationUtils
operator|.
name|readOptionalStringProperty
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|,
name|TAG_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|onFailureProcessorConfigs
operator|!=
literal|null
operator|&&
name|onFailureProcessors
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|type
argument_list|,
name|tag
argument_list|,
name|Pipeline
operator|.
name|ON_FAILURE_KEY
argument_list|,
literal|"processors list cannot be empty"
argument_list|)
throw|;
block|}
try|try
block|{
name|Processor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
name|processorFactories
argument_list|,
name|tag
argument_list|,
name|config
argument_list|)
decl_stmt|;
if|if
condition|(
name|config
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"processor [{}] doesn't support one or more provided configuration parameters {}"
argument_list|,
name|type
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|config
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|onFailureProcessors
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|||
name|ignoreFailure
condition|)
block|{
return|return
operator|new
name|CompoundProcessor
argument_list|(
name|ignoreFailure
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|processor
argument_list|)
argument_list|,
name|onFailureProcessors
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|processor
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|type
argument_list|,
name|tag
argument_list|,
literal|null
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"No processor type exists with name ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit
