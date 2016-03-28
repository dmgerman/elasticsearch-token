begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
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
name|ProcessorsRegistry
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
name|core
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
name|core
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
name|core
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
name|core
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
name|core
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
name|ArrayList
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|ConfigurationUtils
operator|.
name|readList
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|ConfigurationUtils
operator|.
name|readStringProperty
import|;
end_import

begin_comment
comment|/**  * A processor that for each value in a list executes a one or more processors.  *  * This can be useful in cases to do string operations on json array of strings,  * or remove a field from objects inside a json array.  */
end_comment

begin_class
DECL|class|ForEachProcessor
specifier|public
specifier|final
class|class
name|ForEachProcessor
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
literal|"foreach"
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|processors
specifier|private
specifier|final
name|List
argument_list|<
name|Processor
argument_list|>
name|processors
decl_stmt|;
DECL|method|ForEachProcessor
name|ForEachProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|String
name|field
parameter_list|,
name|List
argument_list|<
name|Processor
argument_list|>
name|processors
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
name|processors
operator|=
name|processors
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|ingestDocument
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
name|List
argument_list|<
name|Object
argument_list|>
name|newValues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|values
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|value
range|:
name|values
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|innerSource
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
argument_list|)
decl_stmt|;
name|innerSource
operator|.
name|put
argument_list|(
literal|"_value"
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// scalar value to access the list item being evaluated
name|IngestDocument
name|innerIngestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|innerSource
argument_list|,
name|ingestDocument
operator|.
name|getIngestMetadata
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Processor
name|processor
range|:
name|processors
control|)
block|{
name|processor
operator|.
name|execute
argument_list|(
name|innerIngestDocument
argument_list|)
expr_stmt|;
block|}
name|newValues
operator|.
name|add
argument_list|(
name|innerSource
operator|.
name|get
argument_list|(
literal|"_value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|field
argument_list|,
name|newValues
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
DECL|method|getField
name|String
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
DECL|method|getProcessors
name|List
argument_list|<
name|Processor
argument_list|>
name|getProcessors
parameter_list|()
block|{
return|return
name|processors
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
specifier|final
class|class
name|Factory
extends|extends
name|AbstractProcessorFactory
argument_list|<
name|ForEachProcessor
argument_list|>
block|{
DECL|field|processorRegistry
specifier|private
specifier|final
name|ProcessorsRegistry
name|processorRegistry
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|ProcessorsRegistry
name|processorRegistry
parameter_list|)
block|{
name|this
operator|.
name|processorRegistry
operator|=
name|processorRegistry
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCreate
specifier|protected
name|ForEachProcessor
name|doCreate
parameter_list|(
name|String
name|tag
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
name|readStringProperty
argument_list|(
name|TYPE
argument_list|,
name|tag
argument_list|,
name|config
argument_list|,
literal|"field"
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
name|processorConfigs
init|=
name|readList
argument_list|(
name|TYPE
argument_list|,
name|tag
argument_list|,
name|config
argument_list|,
literal|"processors"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Processor
argument_list|>
name|processors
init|=
name|ConfigurationUtils
operator|.
name|readProcessorConfigs
argument_list|(
name|processorConfigs
argument_list|,
name|processorRegistry
argument_list|)
decl_stmt|;
return|return
operator|new
name|ForEachProcessor
argument_list|(
name|tag
argument_list|,
name|field
argument_list|,
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|processors
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

