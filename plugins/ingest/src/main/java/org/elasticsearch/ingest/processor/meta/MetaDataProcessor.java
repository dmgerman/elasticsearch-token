begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.ingest.processor.meta
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|meta
package|;
end_package

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
name|Mustache
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
name|MustacheFactory
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
name|IngestDocument
operator|.
name|MetaData
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
name|processor
operator|.
name|Processor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
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
name|Iterator
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
DECL|class|MetaDataProcessor
specifier|public
specifier|final
class|class
name|MetaDataProcessor
implements|implements
name|Processor
block|{
DECL|field|TYPE
specifier|public
specifier|final
specifier|static
name|String
name|TYPE
init|=
literal|"meta"
decl_stmt|;
DECL|field|templates
specifier|private
specifier|final
name|Map
argument_list|<
name|MetaData
argument_list|,
name|Mustache
argument_list|>
name|templates
decl_stmt|;
DECL|method|MetaDataProcessor
specifier|public
name|MetaDataProcessor
parameter_list|(
name|Map
argument_list|<
name|MetaData
argument_list|,
name|Mustache
argument_list|>
name|templates
parameter_list|)
block|{
name|this
operator|.
name|templates
operator|=
name|templates
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
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|model
init|=
name|ingestDocument
operator|.
name|getSource
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|MetaData
argument_list|,
name|Mustache
argument_list|>
name|entry
range|:
name|templates
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|execute
argument_list|(
name|writer
argument_list|,
name|model
argument_list|)
expr_stmt|;
name|ingestDocument
operator|.
name|setEsMetadata
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|writer
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
DECL|method|getTemplates
name|Map
argument_list|<
name|MetaData
argument_list|,
name|Mustache
argument_list|>
name|getTemplates
parameter_list|()
block|{
return|return
name|templates
return|;
block|}
DECL|class|Factory
specifier|public
specifier|final
specifier|static
class|class
name|Factory
implements|implements
name|Processor
operator|.
name|Factory
argument_list|<
name|MetaDataProcessor
argument_list|>
block|{
DECL|field|mustacheFactory
specifier|private
specifier|final
name|MustacheFactory
name|mustacheFactory
init|=
operator|new
name|DefaultMustacheFactory
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|create
specifier|public
name|MetaDataProcessor
name|create
parameter_list|(
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
name|Map
argument_list|<
name|MetaData
argument_list|,
name|Mustache
argument_list|>
name|templates
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|iterator
init|=
name|config
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|fromString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|Mustache
name|mustache
init|=
name|mustacheFactory
operator|.
name|compile
argument_list|(
operator|new
name|FastStringReader
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|templates
operator|.
name|put
argument_list|(
name|metaData
argument_list|,
name|mustache
argument_list|)
expr_stmt|;
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|templates
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"no meta fields specified"
argument_list|)
throw|;
block|}
return|return
operator|new
name|MetaDataProcessor
argument_list|(
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|templates
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

