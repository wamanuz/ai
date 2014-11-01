
#include <fstream>
#include <iostream>
#include <string>

int main(const int argc, const char *const *const argv)
{
	if (argc < 2)
	{
		std::cout << "Usage:" << std::endl;
		std::cout << "  sentences2corpus input_file [output_file]" << std::endl;
		return -1;
	}
	const char *const infilename = argv[1];
	const char *const outfilename = argc > 2 ? argv[2] : "out.corpus";

	std::ifstream infile(infilename, std::ios::in | std::ios::binary);
	
	if (infile)
	{
		std::ofstream outfile(outfilename, std::ios::out | std::ios::binary);

		if (outfile)
		{
			std::string line;

			while (std::getline(infile, line))
			{
				unsigned int start = 0;

				while (line[start] != '\t') ++start;
				while (line[start] == '\t') ++start;

				outfile.write(&line[start], line.size() - start);
				outfile.put('\n');
			}
			outfile.close();
		}
		infile.close();
	}
	return 0;
}
