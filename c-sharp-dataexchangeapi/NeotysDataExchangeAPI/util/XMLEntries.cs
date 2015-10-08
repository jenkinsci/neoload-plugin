using System.Collections.Generic;
using System.Text;
using Neotys.DataExchangeAPI.UtilsFromJava;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Rest.Util
{
	using Entry = Neotys.DataExchangeAPI.Model.Entry;
	using EntryBuilder = Neotys.DataExchangeAPI.Model.EntryBuilder;
	using StatusBuilder = Neotys.DataExchangeAPI.Model.StatusBuilder;
	using NeotysAPIException = Neotys.DataExchangeAPI.Error.NeotysAPIException;
	using ErrorType = Neotys.DataExchangeAPI.Error.NeotysAPIException.ErrorType;

	public class XMLEntries
	{

		public const string XMLENTRIES = "XMLEntries";
		public const string XML = "Xml";
		public const string CHARSET = "Charset";
		public const string DEFAULT_CHARSET = "UTF-8";

		public static IList<Entry> FromProperties(IDictionary<string, object> entryProperties)
		{
			IList<string> path = Entries.getPath(entryProperties, false);
			long timestamp = Entries.getTimestamp(entryProperties, false);
			string xml = getXml(entryProperties);
			string charset = getCharset(entryProperties);
			try
			{
				return FromXML(xml, path, timestamp, charset);
			}
			catch (System.Exception e)
			{
				throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Invalid XML content: " + e.Message, e);
			}
		}

		public static IDictionary<string, object> ToProperties(string xml, IList<string> parentPath, long timestamp, string charset)
		{
			IDictionary<string, object> xmlEntriesProperties = new Dictionary<string, object>();
			xmlEntriesProperties[XML] = xml;
			if (parentPath != null)
			{
				xmlEntriesProperties[Entries.Path] = Entries.PathListToString(parentPath);
			}
			xmlEntriesProperties[Entries.Timestamp] = timestamp;
			xmlEntriesProperties[CHARSET] = charset;
			return xmlEntriesProperties;
		}


		protected internal static string getXml(IDictionary<string, object> entryProperties)
		{
			object objectPath = entryProperties[XML];
			if (objectPath == null || System.String.IsNullOrEmpty(objectPath.ToString()))
			{
				throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Missing Xml entry.");
			}
			return objectPath.ToString();
		}

		protected internal static string getCharset(IDictionary<string, object> entryProperties)
		{
			object charset = entryProperties[CHARSET];
			if (charset == null || System.String.IsNullOrEmpty(charset.ToString()))
			{
				return DEFAULT_CHARSET;
			}
			return charset.ToString();
		}


        /// <summary>
        /// Parse an XML content to create a list of <seealso cref="Entry"/>. </summary>
        /// <param name="contentAsXML"> : the xml string containing the XML to parse </param>
        /// <param name="parentPath"> : initial path to add at the begining of all entries </param>
        /// <param name="timestamp"> : the timestamp when the XML file has been computed
        /// @return </param>
        /// <exception cref="ParserConfigurationException"> </exception>
        /// <exception cref="IOException"> </exception>
        /// <exception cref="SAXException"> </exception>
        /// <exception cref="NullPointerException"> if contentAsXML or parentPath is null. </exception>
        public static IList<Entry> FromXML(string contentAsXML, IList<string> parentPath, long timestamp, string charset)
        {

            System.Xml.XmlDocument doc = new System.Xml.XmlDocument();
            doc.LoadXml(contentAsXML);

            return getEntries(doc.ChildNodes, Preconditions.CheckNotNull<IList<string>>(parentPath), timestamp);
        }

		private static IList<Entry> getEntries(System.Xml.XmlNodeList nodeList, IList<string> parentPath, long timestamp)
		{
			IList<Entry> entries = new List<Entry>();
			for (int index = 0 ; index < nodeList.Count ; index++)
			{
                System.Xml.XmlNode node = nodeList.Item(index);
                System.Xml.XmlAttributeCollection attributes = node.Attributes;
				if (node.HasChildNodes)
				{
					IList<string> subPath = new List<string>(parentPath);
					subPath.Add(getNodeNameAndAttributes(node));
					IList<Entry> tmpEntries = getEntries(node.ChildNodes, new List<string>(subPath), timestamp);
					((List<Entry>)entries).AddRange(tmpEntries);
					string text = node.InnerText;
					if (tmpEntries.Count == 0 && !System.String.IsNullOrEmpty(text))
					{
						entries.Add(newEntry(subPath, timestamp, text));
					}
				}
				else if (attributes.Count > 0)
				{
					for (int attributeIndex = 0 ; attributeIndex < attributes.Count ; attributeIndex++)
					{
                        System.Xml.XmlNode attribute = attributes.Item(attributeIndex);
						string attributeName = attribute.Name;
						string attributeValue = attribute.Value;
						if (!System.String.IsNullOrEmpty(attributeName) && !System.String.IsNullOrEmpty(attributeValue))
						{
							IList<string> subPath = new List<string>(parentPath);
							subPath.Add(node.Name + " " + attributeName);
							entries.Add(newEntry(subPath, timestamp, attributeValue));
						}
					}
				}

			}
			return entries;
		}

		private static Entry newEntry(IList<string> parentPath, long timestamp, string text)
		{
			EntryBuilder entryBuilder = new EntryBuilder(parentPath, timestamp);
			try
			{
				double? value = double.Parse(text);
				entryBuilder.Value = value;
			}
			catch (System.Exception e)
			{
                StatusBuilder sb = new StatusBuilder();
                sb.Message = text;
                entryBuilder.Status = sb.Build();
			}
			return entryBuilder.Build();
		}

		private static string getNodeNameAndAttributes(System.Xml.XmlNode node)
		{
			StringBuilder sb = new StringBuilder(node.Name);
            System.Xml.XmlAttributeCollection attributes = node.Attributes;
			for (int index = 0 ; index < attributes.Count ; index++)
			{
				sb.Append(" ").Append(attributes.Item(index).ToString());
			}
			return sb.ToString();
		}
	}

}