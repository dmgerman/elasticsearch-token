begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|common
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|AbstractProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|AbstractProcessorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ConfigurationUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|IngestDocument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|Processor
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

begin_comment
comment|/**  * Processor that sorts an array of items.  * Throws exception is the specified field is not an array.  */
end_comment

begin_class
DECL|class|SortProcessor
specifier|public
specifier|final
class|class
name|SortProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"sort"
decl_stmt|;
DECL|field|FIELD
specifier|public
specifier|static
specifier|final
name|String
name|FIELD
init|=
literal|"field"
decl_stmt|;
DECL|field|ORDER
specifier|public
specifier|static
specifier|final
name|String
name|ORDER
init|=
literal|"order"
decl_stmt|;
DECL|field|DEFAULT_ORDER
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_ORDER
init|=
literal|"asc"
decl_stmt|;
DECL|enum|SortOrder
specifier|public
enum|enum
name|SortOrder
block|{
DECL|enum constant|ASCENDING
DECL|enum constant|DESCENDING
name|ASCENDING
argument_list|(
literal|"asc"
argument_list|)
block|,
name|DESCENDING
argument_list|(
literal|"desc"
argument_list|)
block|;
DECL|field|direction
specifier|private
specifier|final
name|String
name|direction
decl_stmt|;
DECL|method|SortOrder
name|SortOrder
parameter_list|(
name|String
name|direction
parameter_list|)
block|{
name|this
operator|.
name|direction
operator|=
name|direction
expr_stmt|;
block|}
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|direction
return|;
block|}
DECL|method|fromString
specifier|public
specifier|static
name|SortOrder
name|fromString
parameter_list|(
name|String
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
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Sort direction cannot be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|value
operator|.
name|equals
argument_list|(
name|ASCENDING
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|ASCENDING
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|.
name|equals
argument_list|(
name|DESCENDING
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|DESCENDING
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Sort direction ["
operator|+
name|value
operator|+
literal|"] not recognized."
operator|+
literal|" Valid values are: [asc, desc]"
argument_list|)
throw|;
block|}
block|}
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|order
specifier|private
specifier|final
name|SortOrder
name|order
decl_stmt|;
DECL|method|SortProcessor
name|SortProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|String
name|field
parameter_list|,
name|SortOrder
name|order
parameter_list|)
block|{
name|super
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
block|}
DECL|method|getField
name|String
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
DECL|method|getOrder
name|SortOrder
name|getOrder
parameter_list|()
block|{
return|return
name|order
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|document
parameter_list|)
block|{
name|List
argument_list|<
name|?
extends|extends
name|Comparable
argument_list|>
name|list
init|=
name|document
operator|.
name|getFieldValue
argument_list|(
name|field
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field ["
operator|+
name|field
operator|+
literal|"] is null, cannot sort."
argument_list|)
throw|;
block|}
if|if
condition|(
name|list
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|order
operator|.
name|equals
argument_list|(
name|SortOrder
operator|.
name|ASCENDING
argument_list|)
condition|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
name|Collections
operator|.
name|reverseOrder
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|document
operator|.
name|setFieldValue
argument_list|(
name|field
argument_list|,
name|list
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|class|Factory
specifier|public
specifier|final
specifier|static
class|class
name|Factory
extends|extends
name|AbstractProcessorFactory
block|{
annotation|@
name|Override
DECL|method|doCreate
specifier|public
name|SortProcessor
name|doCreate
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|registry
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
name|config
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|field
init|=
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
name|FIELD
argument_list|)
decl_stmt|;
try|try
block|{
name|SortOrder
name|direction
init|=
name|SortOrder
operator|.
name|fromString
argument_list|(
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
name|ORDER
argument_list|,
name|DEFAULT_ORDER
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|SortProcessor
argument_list|(
name|processorTag
argument_list|,
name|field
argument_list|,
name|direction
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
name|ConfigurationUtils
operator|.
name|newConfigurationException
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|ORDER
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

